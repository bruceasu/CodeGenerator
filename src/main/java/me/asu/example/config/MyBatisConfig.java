package me.asu.example.config;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.baomidou.mybatisplus.entity.GlobalConfiguration;
import com.baomidou.mybatisplus.mapper.ISqlInjector;
import com.baomidou.mybatisplus.mapper.LogicSqlInjector;
import com.baomidou.mybatisplus.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import javax.sql.DataSource;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//import org.springframework.core.io.Resource;

/**
 * Mybatis & Mapper & PageHelper 配置
 *
 * @author chenbin on 2017/12/25
 * @version 3.0.0
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {"com.example.dal.mapper"})
public class MyBatisConfig {

	/**
	 * mybatis-plus分页插件<br>
	 * 文档：http://mp.baomidou.com<br>
	 */
	@Bean
	public PaginationInterceptor paginationInterceptor() {
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		// 开启 PageHelper 的支持
		// paginationInterceptor.setLocalPage(true);
		return paginationInterceptor;
	}

	@Bean
	public OptimisticLockerInterceptor optimisticLockerInterceptor() {
		OptimisticLockerInterceptor interceptor = new OptimisticLockerInterceptor();
		return interceptor;
	}

	/**
	 * mybatis-plus SQL执行效率插件【生产环境可以关闭】
	 */
	@Bean
	@Profile({"dev", "sit", "uat"})
	public PerformanceInterceptor performanceInterceptor() {
		PerformanceInterceptor interceptor = new PerformanceInterceptor();
		return interceptor;
	}

	/**
	 * 注入sql注入器
	 */
	@Bean
	public ISqlInjector sqlInjector() {
		return new LogicSqlInjector();
	}


	@Bean("mybatisSqlSession")
	public SqlSessionFactory sqlSessionFactory(
			DataSource dataSource,
			ResourceLoader resourceLoader,
			GlobalConfiguration globalConfiguration,
			PaginationInterceptor paginationInterceptor) throws Exception {
		MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
//		PathMatchingResourcePatternResolver resolverOfConfig = new PathMatchingResourcePatternResolver();
//		Resource resource = resolverOfConfig.getResource("classpath:mybatis-config.xml");
//		sqlSessionFactoryBean.setConfigLocation(resource);
		// 添加XML目录, 支持通配
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(
				"classpath*:com/example/dal/mapper/xml/*.xml");
		sqlSessionFactoryBean.setMapperLocations(resources);
		// 实体扫描，多个package用逗号或者分号分隔, 支持通配
		sqlSessionFactoryBean.setTypeAliasesPackage("com.example.dal.entity");
		// sqlSessionFactory.setTypeEnumsPackage();
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		// 查询时，关闭关联对象即时加载以提高性能
		configuration.setLazyLoadingEnabled(true);
		configuration.setDefaultStatementTimeout(25000);
		configuration.setLogImpl(Slf4jImpl.class);

		// 以下是默认值
		//configuration.setDefaultScriptingLanguage(MybatisXMLLanguageDriver.class);
		// 全局映射器启用缓存
		//configuration.setCacheEnabled(true);
		// 设置关联对象加载的形态，此处为按需加载字段(加载字段由SQL指定)，不会加载关联表的所有字段，以提高性能
		//configuration.setAggressiveLazyLoading(false);
		// 对于未知的SQL查询，允许返回不同的结果集以达到通用的效果
		//configuration.setMultipleResultSetsEnabled(true);
		// 允许使用列标签代替列名
		// configuration.setUseColumnLabel(true);
		sqlSessionFactoryBean.setConfiguration(configuration);
//		sqlSessionFactory.setPlugins(new Interceptor[]{
//				paginationInterceptor,
//				// mybatis-plus SQL执行效率插件【生产环境可以关闭】
//				new PerformanceInterceptor(),
//				new OptimisticLockerInterceptor()
//		});
		sqlSessionFactoryBean.setGlobalConfig(globalConfiguration);
		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public GlobalConfiguration globalConfiguration() {
		GlobalConfiguration conf = new GlobalConfiguration();
		// 逻辑删除 定义下面3个参数
		// 自定义sql注入器,不在推荐使用此方式进行配置,请使用自定义bean注入
		// conf.setSqlInjector(new LogicSqlInjector());
		conf.setLogicDeleteValue("Y");
		conf.setLogicNotDeleteValue("N");
		// 全局ID类型：
		// 0, "数据库ID自增"，
		// 1, "用户输入ID",
		// 2, "全局唯一ID",
		// 3, "全局唯一ID"
		//	idType
		//		描述：定义主键策略
		//		类型：Enum
		//		默认值：IdType.ID_WORKER
		//		可选值：AUTO（数据库自增）、INPUT(自行输入)、ID_WORKER（分布式全局唯一ID）、UUID（32位UUID字符串）、ID_WORKER_STR（分布式全局唯一ID 字符串类型）
		//		Java	                XML
		//		IdType.AUTO	            0
		//		IdType.INPUT	        1
		//		IdType.ID_WORKER	    2
		//		IdType.UUID	            3
		//		IdType.NONE	            4
		//		IdType.ID_WORKER_STR	5

		conf.setIdType(0);
		// idType = 2
		// 序列接口实现类配置,不在推荐使用此方式进行配置,请使用自定义bean注入
		// conf.setKeyGenerator(new OracleKeyGenerator());
		// 自定义填充策略接口实现,不在推荐使用此方式进行配置,请使用自定义bean注入
		// conf.setMetaObjectHandler(new MyMetaObjectHandler());
		conf.setCapitalMode(true);
		// 刷新mapper 调试神器
		conf.setRefresh(true);
		// 驼峰下划线转换
		conf.setDbColumnUnderline(true);
		// 字段策略 0:"忽略判断",1:"非 NULL 判断"),2:"非空判断"
		conf.setFieldStrategy(2);
		//  SQL 解析缓存，开启后多租户 @SqlParser 注解生效
		conf.setSqlParserCache(true);
		return conf;
	}

	@Bean
	public static PlatformTransactionManager txManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}


}

