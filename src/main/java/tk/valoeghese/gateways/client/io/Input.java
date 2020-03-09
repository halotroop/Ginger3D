package tk.valoeghese.gateways.client.io;

import java.util.*;

/** Author: Valoeghese */
public class Input
{
	private static final Map<Keybind, List<KeyCallback>> CALLBACKS = new HashMap<>();
	private static final Map<Keybind, List<KeyListener>> LISTENERS = new HashMap<>();

	public static void addListener(Keybind key, KeyListener callback)
	{ LISTENERS.computeIfAbsent(key, listener -> new ArrayList<>()).add(callback); }

	public static void addPressCallback(Keybind key, KeyCallback callback)
	{ CALLBACKS.computeIfAbsent(key, listener -> new ArrayList<>()).add(callback); }

	public static void addInitialPressCallback(Keybind key, KeyCallback callback)
	{ addListener(key, new InitialPressHandler(callback)); }

	public static void invokeAllListeners()
	{
		CALLBACKS.forEach((keybind, listeners) ->
		{
			if (keybind.isActive())
			{ listeners.forEach(callback -> callback.onCallback()); }
		});
		LISTENERS.forEach((keybind, listeners) ->
		{
			listeners.forEach(listener -> listener.listen(keybind.isActive()));
		});
	}
}
