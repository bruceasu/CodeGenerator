package me.asu.example.generator;

/**
 * 项目常量
 * @author suk
 */
public final class ProjectConstant {

	/**
	 * 获取当前类的路径
	 */
	private static String packAge = ProjectConstant.class.getPackage().getName();


	/**
	 * Domain所在包
	 */
	public static final String DOMAIN_PACKAGE = "dal.entity";
	/**
	 * Mapper所在包
	 */
	public static final String MAPPER_PACKAGE = "dal.mapper";
	/**
	 * Mapper所在包
	 */
	public static final String MAPPER_XML_PACKAGE = "dal.mapper.xml";
	/**
	 * Service所在包
	 */
	public static final String SERVICE_PACKAGE = "service";
	/**
	 * ServiceImpl所在包
	 */
	public static final String SERVICE_IMPL_PACKAGE = "service.impl";

	/**
	 * Controller所在包
	 */
	public static final String CONTROLLER_PACKAGE = "controller";


	/**
	 * SwaggerUI的所在包
	 */
	public static String SWAGGER_PACKAGE;


	/**
	 * 根据路径切割最后一位名称获得当前类上一级路径
	 */
	public static String MODULE_NAME;

	/**
	 * 项目基础包名称，根据项目修改
	 */
	public static String BASE_PACKAGE;


	static {
		String pkg = packAge.substring(0, packAge.lastIndexOf("."));
		BASE_PACKAGE = pkg.substring(0, pkg.lastIndexOf("."));
		MODULE_NAME = pkg.substring(BASE_PACKAGE.length()+1);
		SWAGGER_PACKAGE = BASE_PACKAGE + "." + MODULE_NAME + "." + CONTROLLER_PACKAGE;
	}

}
