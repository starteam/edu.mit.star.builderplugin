package star.annotations;

public @interface Wrap {
	enum Types { None, SwingUtilitiesInvokeLater }
	public Class<? extends Object> value() default Runnable.class ;
	public String method() default "";
	public Types type() default Types.None ;
}
