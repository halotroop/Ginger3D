package tk.valoeghese.gateways.client.io;

/*
 * Author: Valoeghese
 */
public final class InitialPressHandler implements KeyListener
{
	private boolean activatedPreviously = false;
	private final KeyCallback callback;

	public InitialPressHandler(KeyCallback callback)
	{ this.callback = callback; }

	@Override
	public void listen(boolean active)
	{
		if (!activatedPreviously && active)
		{ callback.onCallback(); }
		activatedPreviously = active;
	}
}
