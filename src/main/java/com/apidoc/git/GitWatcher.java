package com.apidoc.git;

import com.apidoc.config.ApiDocConfig;
import com.apidoc.generator.DocumentGenerator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class GitWatcher {
    
    private final DocumentGenerator documentGenerator;
    private final ApiDocConfig config;
    
    private volatile long lastCommitTime = 0;
    
    public GitWatcher(DocumentGenerator documentGenerator, ApiDocConfig config) {
        this.documentGenerator = documentGenerator;
        this.config = config;
    }
    
    @Scheduled(fixedDelay = 60000)
    public void watchForChanges() {
        if (!config.getGit().isEnabled()) {
            return;
        }
        
        try {
            Path repoPath = Paths.get(config.getGit().getRepoPath());
            File gitDir = new File(repoPath.toFile(), ".git");
            
            if (!gitDir.exists()) {
                log.debug("未找到Git仓库: {}", gitDir.getAbsolutePath());
                return;
            }
            
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitDir).build();
            
            Git git = new Git(repository);
            
            String currentBranch = repository.getBranch();
            log.debug("当前分支: {}", currentBranch);
            
            long latestCommitTime = getLatestCommitTime(git);
            
            if (latestCommitTime > lastCommitTime) {
                log.info("检测到代码变更，重新生成文档...");
                documentGenerator.generateDocumentation();
                lastCommitTime = latestCommitTime;
            }
            
            git.close();
            repository.close();
            
        } catch (Exception e) {
            log.error("Git监听失败", e);
        }
    }
    
    private long getLatestCommitTime(Git git) throws Exception {
        return git.log()
                .setMaxCount(1)
                .call()
                .iterator()
                .next()
                .getCommitTime() * 1000L;
    }
}
