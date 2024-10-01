package gov.geoplatform.knowstac.web.config;

import java.util.concurrent.Executor;

import javax.servlet.Filter;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import gov.geoplatform.knowstac.web.controller.Base64QueryCriteriaConverter;
import gov.geoplatform.knowstac.web.service.SessionFilter;
import net.geoprism.EncodingFilter;
import net.geoprism.spring.JsonExceptionHandler;

// Configuration of components which DO have web container dependencies
@Configuration
@EnableWebMvc
@ComponentScan(
    basePackages = { 
        "gov.geoplatform.knowstac.core.config", 
        "gov.geoplatform.knowstac.web.config", 
        "gov.geoplatform.knowstac.web.service", 
        "gov.geoplatform.knowstac.web.controller",
        "net.geoprism.registry.spring", 
        "net.geoprism.registry.service", 
},  excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = {
        net.geoprism.registry.service.SessionFilter.class
})  )
public class WebConfig implements WebMvcConfigurer, AsyncConfigurer
{

  @Bean
  Filter sessionFilter()
  {
    return new SessionFilter();
  }

  @Bean
  JsonExceptionHandler errorHandler()
  {
    return new JsonExceptionHandler();
  }

  @Bean
  EncodingFilter encodingFilter()
  {
    return new EncodingFilter();
  }

  @Override
  @Bean(name = "taskExecutor")
  public Executor getAsyncExecutor()
  {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("async");
    return executor;
  }

  // ---------------> Use this task executor also for async rest methods
  @Bean
  protected WebMvcConfigurer webMvcConfigurer()
  {
    return new WebMvcConfigurer()
    {
      @Override
      public void configureAsyncSupport(AsyncSupportConfigurer configurer)
      {
        configurer.setTaskExecutor(getTaskExecutor());
      }
    };
  }

  @Bean
  protected ConcurrentTaskExecutor getTaskExecutor()
  {
    return new ConcurrentTaskExecutor(this.getAsyncExecutor());
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
  {
    return new SimpleAsyncUncaughtExceptionHandler();
  }

  @Override
  public void addFormatters(FormatterRegistry registry)
  {
    registry.addConverter(new Base64QueryCriteriaConverter());
  }

  @Bean(name = "multipartResolver")
  public CommonsMultipartResolver multipartResolver()
  {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    multipartResolver.setMaxUploadSize(-1);
    return multipartResolver;
  }

}