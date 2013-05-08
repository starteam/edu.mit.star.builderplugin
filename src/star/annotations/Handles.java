package star.annotations;

import star.event.Raiser;

public @interface Handles
{
	public Class<? extends Raiser>[] raises();

	public boolean handleValid() default true;
}
