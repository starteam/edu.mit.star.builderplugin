package star.annotations;

public @interface Preferences
{
	String DEFAULT = "DEFAULT\0";

	public String prefix() default DEFAULT;
	
	public String loadResource() default "" ;
	
	public String application() default "" ;
}
