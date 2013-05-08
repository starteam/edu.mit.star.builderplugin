package star.annotations;

import star.event.Raiser;

public @interface SignalComponent
{
	public Class<? extends Object> extend() default Object.class;

	public Class<? extends Raiser>[] handles() default {};

	public Class<? extends Raiser>[] contains() default {};

	public Class<? extends Raiser>[] excludeExternal() default {};

	public Class<? extends Raiser>[] excludeInternal() default {};

}
