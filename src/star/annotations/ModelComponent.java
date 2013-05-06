package star.annotations;

public @interface ModelComponent
{
	public Class<? extends Object> extend() default Object.class;
	public Observable[] value();
}
