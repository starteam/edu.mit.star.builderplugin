package star.annotations;

public @interface Observable
{
	public Class<?> type() default Observable.class;
	public String name() default "" ;
}
