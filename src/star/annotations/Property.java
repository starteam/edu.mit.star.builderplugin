package star.annotations;

public @interface Property
{
	final static int DEFAULT = -1;
	final static int NOT_GENERATED = 0;
	final static int PUBLIC = 1;
	final static int PROTECTED = 2;

	public int getter() default PROTECTED;

	public int setter() default PROTECTED;

	public Class type() default Property.class;

	public String name() default "";

	public String value() default "";

	public String params() default "";
}
