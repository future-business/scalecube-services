package io.scalecube.services.methods;

import io.scalecube.services.Reflect;
import io.scalecube.services.exceptions.ServiceProviderErrorMapper;
import io.scalecube.services.registry.ServiceRegistryImpl;
import io.scalecube.services.transport.api.ServiceMessageDataDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ServiceMethodRegistryImpl implements ServiceMethodRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMethodRegistryImpl.class);

  private final ConcurrentMap<String, ServiceMethodInvoker> methodInvokers =
      new ConcurrentHashMap<>();

  @Override
  public void registerService(
      Object serviceInstance,
      ServiceProviderErrorMapper errorMapper,
      ServiceMessageDataDecoder dataDecoder) {
    Reflect.serviceInterfaces(serviceInstance)
        .forEach(
            serviceInterface ->
                Reflect.serviceMethods(serviceInterface)
                    .forEach(
                        (key, method) -> {

                          // validate method
                          Reflect.validateMethodOrThrow(method);

                          MethodInfo methodInfo =
                              new MethodInfo(
                                  Reflect.serviceName(serviceInterface),
                                  Reflect.methodName(method),
                                  Reflect.parameterizedReturnType(method),
                                  Reflect.communicationMode(method),
                                  method.getParameterCount(),
                                  Reflect.requestType(method));

                          // register new service method invoker
                          String qualifier = methodInfo.qualifier();
                          ServiceMethodInvoker invoker = new ServiceMethodInvoker(
                                  method, serviceInstance, methodInfo, errorMapper, dataDecoder);

                          if (methodInvokers.containsKey(qualifier)) {
                            LOGGER.warn("<{}> for API <{}> will be replaced by the following methodInvoker: <{}>", methodInvokers.get(qualifier), qualifier, invoker);
                          }
                          methodInvokers.put(methodInfo.qualifier(), invoker);
                        }));
  }

  @Override
  public boolean containsInvoker(String qualifier) {
    return methodInvokers.containsKey(qualifier);
  }

  @Override
  public ServiceMethodInvoker getInvoker(String qualifier) {
    return methodInvokers.get(qualifier);
  }
}
