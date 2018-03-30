package me.asu.example.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import me.asu.example.generator.core.Result;
import me.asu.example.generator.core.ResultCode;
import me.asu.example.generator.core.ServiceException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring MVC 配置
 *
 * @author chenbin on 2017/12/25
 * @version 3.0.0
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	private final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);
	@Value("${spring.profiles.active}")
	private String env; // 当前激活的配置文件

	/**
	 * 解决路径资源映射问题
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html")
				.addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	/**
	 * 使用阿里 FastJson 作为JSON MessageConverter
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
		FastJsonConfig config = new FastJsonConfig();
		config.setSerializerFeatures(SerializerFeature.WriteMapNullValue,//保留空的字段
				SerializerFeature.WriteNullStringAsEmpty,//String null -> ""
				SerializerFeature.WriteNullNumberAsZero);//Number null -> 0
		converter.setFastJsonConfig(config);
		converter.setDefaultCharset(Charset.forName("UTF-8"));
		converters.add(converter);
	}

	/**
	 * 统一异常处理
	 */
	@Override
	public void configureHandlerExceptionResolvers(
			List<HandlerExceptionResolver> exceptionResolvers) {
		exceptionResolvers.add((request, response, handler, e) -> {
			Result result = new Result();
			if (e instanceof ServiceException) {
				//业务失败的异常
				result.setResCode(ResultCode.FAIL.code());
				result.setResMsg(e.getMessage());
				logger.info(e.getMessage());
			} else if (e instanceof NoHandlerFoundException) {
				result.setResCode(ResultCode.NOT_FOUND.code());
				result.setResMsg("接口 [" + request.getRequestURI() + "] 不存在");
			} else if (e instanceof ServletException) {
				result.setResCode(ResultCode.FAIL.code());
				result.setResMsg(e.getMessage());
			} else {
				result.setResCode(ResultCode.INTERNAL_SERVER_ERROR.code());
				result.setResMsg("接口 [" + request.getRequestURI() + "] 内部错误，请联系管理员");
				String message;
				if (handler instanceof HandlerMethod) {
					HandlerMethod handlerMethod = (HandlerMethod) handler;
					message = String.format("接口 [%s] 出现异常，方法：%s.%s，异常摘要：%s",
							request.getRequestURI(),
							handlerMethod.getBean().getClass().getName(),
							handlerMethod.getMethod().getName(),
							e.getMessage());
				} else {
					message = e.getMessage();
				}
				logger.error(message, e);
			}
			responseResult(response, result);
			return new ModelAndView();
		});
	}

	/**
	 * 解决跨域问题
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**");
	}

	/**
	 * 添加拦截器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {

	}

	private void responseResult(HttpServletResponse response, Result result) {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "application/json;charset=UTF-8");
		response.setStatus(200);
		try {
			response.getWriter().write(JSON.toJSONString(result));
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
	}



	private String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// 如果是多级代理，那么取第一个ip为客户端ip
		if (ip != null && ip.indexOf(",") != -1) {
			ip = ip.substring(0, ip.indexOf(",")).trim();
		}
		return ip;
	}
}
