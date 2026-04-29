package com.apidoc.scanner;

import com.apidoc.config.ApiDocConfig;
import com.apidoc.model.ApiEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Slf4j
@Component
public class ControllerScanner {
    
    private final ApiDocConfig config;
    
    public ControllerScanner(ApiDocConfig config) {
        this.config = config;
    }
    
    public List<ApiEndpoint> scanControllers() {
        log.info("开始扫描Controller: {}", config.getScanPackage());
        
        Reflections reflections = new Reflections(config.getScanPackage());
        
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(RestController.class);
        Set<Class<?>> mvcControllers = reflections.getTypesAnnotatedWith(Controller.class);
        controllerClasses.addAll(mvcControllers);
        
        log.info("找到 {} 个Controller类", controllerClasses.size());
        
        List<ApiEndpoint> endpoints = new ArrayList<>();
        
        for (Class<?> controllerClass : controllerClasses) {
            try {
                endpoints.addAll(scanControllerClass(controllerClass));
            } catch (Exception e) {
                log.error("扫描Controller失败: {}", controllerClass.getName(), e);
            }
        }
        
        log.info("扫描完成，共发现 {} 个接口端点", endpoints.size());
        return endpoints;
    }
    
    private List<ApiEndpoint> scanControllerClass(Class<?> controllerClass) {
        List<ApiEndpoint> endpoints = new ArrayList<>();
        
        String classMapping = getClassMapping(controllerClass);
        String classDescription = getClassDescription(controllerClass);
        
        Method[] methods = controllerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            ApiEndpoint endpoint = scanMethod(method, controllerClass, classMapping, classDescription);
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }
        
        return endpoints;
    }
    
    private ApiEndpoint scanMethod(Method method, Class<?> controllerClass, 
                                   String classMapping, String classDescription) {
        RequestMapping requestMapping = getRequestMapping(method);
        
        if (requestMapping == null) {
            return null;
        }
        
        String httpMethod = determineHttpMethod(method, requestMapping);
        String path = buildPath(classMapping, requestMapping);
        
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        
        return ApiEndpoint.builder()
                .className(controllerClass.getSimpleName())
                .classDescription(classDescription)
                .methodName(method.getName())
                .methodDescription(getMethodDescription(method))
                .httpMethod(httpMethod)
                .path(path)
                .parameters(extractParameters(method))
                .responseInfo(extractResponseInfo(method))
                .errorCodes(new ArrayList<>())
                .tags(extractTags(controllerClass, method))
                .build();
    }
    
    private RequestMapping getRequestMapping(Method method) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            return mapping;
        }
        
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return convertToRequestMapping(getMapping.value(), getMapping.path(), RequestMethod.GET);
        }
        
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            return convertToRequestMapping(postMapping.value(), postMapping.path(), RequestMethod.POST);
        }
        
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            return convertToRequestMapping(putMapping.value(), putMapping.path(), RequestMethod.PUT);
        }
        
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            return convertToRequestMapping(deleteMapping.value(), deleteMapping.path(), RequestMethod.DELETE);
        }
        
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            return convertToRequestMapping(patchMapping.value(), patchMapping.path(), RequestMethod.PATCH);
        }
        
        return null;
    }
    
    private RequestMapping convertToRequestMapping(String[] value, String[] path, RequestMethod method) {
        return new RequestMapping() {
            @Override
            public String[] value() {
                return path.length > 0 ? path : value;
            }
            
            @Override
            public String[] path() {
                return path;
            }
            
            @Override
            public RequestMethod[] method() {
                return new RequestMethod[]{method};
            }
            
            @Override
            public String name() {
                return "";
            }
            
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestMapping.class;
            }
            
            @Override
            public String[] params() {
                return new String[0];
            }
            
            @Override
            public String[] headers() {
                return new String[0];
            }
            
            @Override
            public String[] consumes() {
                return new String[0];
            }
            
            @Override
            public String[] produces() {
                return new String[0];
            }
        };
    }
    
    private String getClassMapping(Class<?> controllerClass) {
        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        if (mapping != null && mapping.value().length > 0) {
            return mapping.value()[0];
        }
        return "";
    }
    
    private String getClassDescription(Class<?> controllerClass) {
        return controllerClass.getSimpleName();
    }
    
    private String getMethodDescription(Method method) {
        return method.getName();
    }
    
    private String determineHttpMethod(Method method, RequestMapping mapping) {
        RequestMethod[] methods = mapping.method();
        if (methods.length > 0) {
            return methods[0].name();
        }
        
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        }
        
        return "GET";
    }
    
    private String buildPath(String classMapping, RequestMapping methodMapping) {
        String methodPath = "";
        String[] paths = methodMapping.path();
        if (paths.length > 0) {
            methodPath = paths[0];
        } else {
            String[] values = methodMapping.value();
            if (values.length > 0) {
                methodPath = values[0];
            }
        }
        
        if (StringUtils.isEmpty(classMapping)) {
            return methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        }
        
        if (StringUtils.isEmpty(methodPath)) {
            return classMapping;
        }
        
        // 移除重复的斜杠
        String cleanClassMapping = classMapping.endsWith("/") ? classMapping.substring(0, classMapping.length() - 1) : classMapping;
        String cleanMethodPath = methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        
        return cleanClassMapping + cleanMethodPath;
    }
    
    private List<ApiEndpoint.Parameter> extractParameters(Method method) {
        List<ApiEndpoint.Parameter> parameters = new ArrayList<>();
        
        Parameter[] methodParameters = method.getParameters();
        
        for (Parameter parameter : methodParameters) {
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                continue;
            }
            
            String paramName = getParamName(parameter);
            String paramType = parameter.getType().getSimpleName();
            String location = getParameterLocation(parameter);
            boolean required = isParameterRequired(parameter);
            
            parameters.add(ApiEndpoint.Parameter.builder()
                    .name(paramName)
                    .type(paramType)
                    .location(location)
                    .required(required)
                    .description(paramName)
                    .example("")
                    .build());
        }
        
        return parameters;
    }
    
    private String getParamName(Parameter parameter) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null && StringUtils.isNotEmpty(requestParam.value())) {
            return requestParam.value();
        }
        
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null && StringUtils.isNotEmpty(pathVariable.value())) {
            return pathVariable.value();
        }
        
        return parameter.getName();
    }
    
    private String getParameterLocation(Parameter parameter) {
        if (parameter.isAnnotationPresent(PathVariable.class)) {
            return "path";
        } else if (parameter.isAnnotationPresent(RequestHeader.class)) {
            return "header";
        } else if (parameter.isAnnotationPresent(CookieValue.class)) {
            return "cookie";
        }
        return "query";
    }
    
    private boolean isParameterRequired(Parameter parameter) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            return requestParam.required();
        }
        
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return pathVariable.required();
        }
        
        return true;
    }
    
    private ApiEndpoint.ResponseInfo extractResponseInfo(Method method) {
        Class<?> returnType = method.getReturnType();
        
        return ApiEndpoint.ResponseInfo.builder()
                .type(returnType.getSimpleName())
                .description("响应数据")
                .example("")
                .build();
    }
    
    private List<String> extractTags(Class<?> controllerClass, Method method) {
        List<String> tags = new ArrayList<>();
        tags.add(controllerClass.getSimpleName());
        return tags;
    }
}
