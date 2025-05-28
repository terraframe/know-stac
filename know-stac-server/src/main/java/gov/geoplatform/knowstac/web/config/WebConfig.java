package gov.geoplatform.knowstac.web.config;

import java.util.concurrent.Executor;

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
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import gov.geoplatform.knowstac.web.controller.Base64QueryCriteriaConverter;
import gov.geoplatform.knowstac.web.service.SessionFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import net.geoprism.EncodingFilter;
import net.geoprism.spring.web.JsonExceptionHandler;

// Configuration of components which DO have web container dependencies
@Configuration
@EnableWebMvc
@ComponentScan(
    basePackages = { 
        "gov.geoplatform.knowstac.core.config", 
        "gov.geoplatform.knowstac.web.config", 
        "gov.geoplatform.knowstac.web.service", 
        "gov.geoplatform.knowstac.web.controller",
        "net.geoprism.registry.service",
        "net.geoprism.spring.web"
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
  public MultipartResolver multipartResolver()
  {
    StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
    return multipartResolver;
  }

  @Bean
  public MultipartConfigElement multipartConfigElement() {
      // Unlimited file size (-1 for maxFileSize and maxRequestSize)
      return new MultipartConfigElement(null, -1, -1, 0);
  }

}