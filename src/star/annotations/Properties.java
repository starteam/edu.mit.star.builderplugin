package star.annotations;

public @interface Properties
{
	public Property[] value();
	public boolean propertyChangeListener() default false;
}
