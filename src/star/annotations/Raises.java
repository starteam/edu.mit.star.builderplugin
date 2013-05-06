package star.annotations;

public @interface Raises
{
	public Class<? extends star.event.Raiser>[] value();
}
