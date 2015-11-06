/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import net.imagej.ops.OpCandidate.StatusCode;

import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.PluginInfo;
import org.scijava.util.ClassUtils;

/**
 * Utility methods for working with ops. In particular, this class contains
 * handy methods for generating human-readable strings describing ops and match
 * requests against them.
 * 
 * @author Curtis Rueden
 */
public final class OpUtils {

	private OpUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	/** Gets the name of the given op. */
	public static String getName(final ModuleInfo info) {
		final String name = info.getName();
		if (name != null && !name.isEmpty()) return name;

		// name not explicitly specified; look for NAME constant
		return getFieldValue(info, String.class, "NAME");
	}

	/** Gets the aliases associated with the given op. */
	public static String[] getAliases(final ModuleInfo info) {
		// check for an alias
		final String alias = info.get("alias");
		if (alias != null) return new String[] { alias };

		// no single alias; check for a list of aliases
		final String aliases = info.get("aliases");
		if (aliases != null) return aliases.split("\\s*,\\s*");

		// alias not explicitly specified; look for ALIAS constant
		final String aliasField = getFieldValue(info, String.class, "ALIAS");
		if (aliasField != null) return new String[] {aliasField};

		// no single alias; look for ALIASES constant
		final String aliasesField = getFieldValue(info, String.class, "ALIASES");
		if (aliasesField != null) return aliasesField.split("\\s*,\\s*");

		return null;
	}

	/** Gets the namespace portion of the given op name. */
	public static String getNamespace(final String opName) {
		if (opName == null) return null;
		final int dot = opName.lastIndexOf(".");
		return dot < 0 ? null : opName.substring(0, dot);
	}

	/** Gets the simple portion (without namespace) of the given op name. */
	public static String stripNamespace(final String opName) {
		if (opName == null) return null;
		final int dot = opName.lastIndexOf(".");
		return dot < 0 ? opName : opName.substring(dot + 1);
	}

	/**
	 * Unwraps the delegate object of the given {@link Module}, ensuring it is an
	 * instance whose type matches the specified {@link OpRef}.
	 * 
	 * @param module The module to unwrap.
	 * @param ref The {@link OpRef} defining the op's type restrictions.
	 * @return The unwrapped {@link Op}.
	 * @throws IllegalStateException if the op does not conform to the expected
	 *           types.
	 */
	public static <OP extends Op> OP unwrap(final Module module,
		final OpRef<OP> ref)
	{
		return unwrap(module, ref.getType(), ref.getTypes());
	}

	/**
	 * Unwraps the delegate object of the given {@link Module}, ensuring it is an
	 * instance of the specified type(s).
	 * 
	 * @param module The module to unwrap.
	 * @param type The expected type of {@link Op}.
	 * @param types Other required types for the op (e.g., {@link ComputerOp}).
	 * @return The unwrapped {@link Op}.
	 * @throws IllegalStateException if the op does not conform to the expected
	 *           types.
	 */
	public static <OP extends Op> OP unwrap(final Module module,
		final Class<OP> type, final Collection<? extends Class<?>> types)
	{
		if (module == null) return null;
		final Object delegate = module.getDelegateObject();
		final Class<?> opType = type == null ? Op.class : type;
		if (!opType.isInstance(delegate)) {
			throw new IllegalStateException(delegate.getClass().getName() +
				" is not of type " + opType.getName());
		}
		if (types != null) {
			for (final Class<?> t : types) {
				if (!t.isInstance(delegate)) {
					throw new IllegalStateException(delegate.getClass().getName() +
						" is not of type " + t.getName());
				}
			}
		}
		@SuppressWarnings("unchecked")
		final OP op = (OP) delegate;
		return op;
	}

	/**
	 * Gets a string describing the given op request.
	 * 
	 * @param name The op's name.
	 * @param args The op's input arguments.
	 * @return A string describing the op request.
	 */
	public static String opString(final String name, final Object... args) {
		final StringBuilder sb = new StringBuilder();
		sb.append(name + "(\n\t\t");
		boolean first = true;
		for (final Object arg : args) {
			if (first) first = false;
			else sb.append(",\n\t\t");
			if (arg == null) sb.append("null");
			else if (arg instanceof Class) {
				// NB: Class instance used to mark argument type.
				sb.append(((Class<?>) arg).getSimpleName());
			}
			else sb.append(arg.getClass().getSimpleName());
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Gets a string describing the given op.
	 * 
	 * @param info The {@link ModuleInfo} metadata which describes the op.
	 * @return A string describing the op.
	 */
	public static String opString(final ModuleInfo info) {
		return opString(info, null);
	}

	/**
	 * Gets a string describing the given op, highlighting the specific parameter.
	 * 
	 * @param info The {@link ModuleInfo} metadata which describes the op.
	 * @param special A parameter of particular interest when describing the op.
	 * @return A string describing the op.
	 */
	public static String opString(final ModuleInfo info,
		final ModuleItem<?> special)
	{
		final StringBuilder sb = new StringBuilder();
		final String outputString = paramString(info.outputs(), null).trim();
		if (!outputString.isEmpty()) sb.append("(" + outputString + ") =\n\t");
		sb.append(info.getDelegateClassName());
		sb.append("(" + paramString(info.inputs(), special) + ")");
		return sb.toString();
	}

	public static <OP extends Op> String matchInfo(
		final List<OpCandidate<OP>> candidates, final List<Module> matches)
	{
		final StringBuilder sb = new StringBuilder();

		final OpRef<OP> ref = candidates.get(0).getRef();
		if (matches.isEmpty()) {
			// no matches
			sb.append("No matching '" + ref.getLabel() + "' op\n");
		}
		else {
			// multiple matches
			final double priority = matches.get(0).getInfo().getPriority();
			sb.append("Multiple '" + ref.getLabel() + "' ops of priority " +
				priority + ":\n");
			int count = 0;
			for (final Module module : matches) {
				sb.append(++count + ". ");
				sb.append(opString(module.getInfo()) + "\n");
			}
		}

		// fail, with information about the request and candidates
		sb.append("\n");
		sb.append("Request:\n");
		sb.append("-\t" + opString(ref.getLabel(), ref.getArgs()) + "\n");
		sb.append("\n");
		sb.append("Candidates:\n");
		int count = 0;
		for (final OpCandidate<OP> candidate : candidates) {
			final ModuleInfo info = candidate.getInfo();
			sb.append(++count + ". ");
			sb.append("\t" + opString(info, candidate.getStatusItem()) + "\n");
			final String status = candidate.getStatus();
			if (status != null) sb.append("\t" + status + "\n");
			if (candidate.getStatusCode() == StatusCode.DOES_NOT_CONFORM) {
				// show argument values when a contingent op rejects them
				for (final ModuleItem<?> item : info.inputs()) {
					final Object value = item.getValue(candidate.getModule());
					sb.append("\t\t" + item.getName() + " = " + value + "\n");
				}
			}
		}
		return sb.toString();
	}

	// -- Helper methods --

	/** Helper method of {@link #getName} and {@link #getAliases}. */
	private static <T> T getFieldValue(final ModuleInfo info,
		final Class<T> fieldType, final String fieldName)
	{
		if (!(info instanceof PluginInfo)) return null;
		final PluginInfo<?> pInfo = (PluginInfo<?>) info;

		// HACK: The CommandInfo.getPluginType() method returns Command.class.
		// But we want to get the actual type of the wrapped PluginInfo.
		// CommandInfo does not have a getPluginInfo() unwrap method, though.
		// So let's extract the type directly from the @Plugin annotation.
		final Class<?> opType = pInfo.getAnnotation().type();

		final Field nameField = ClassUtils.getField(opType, fieldName);
		if (nameField == null) return null;
		if (!fieldType.isAssignableFrom(nameField.getType())) return null;
		@SuppressWarnings("unchecked")
		final T value = (T) ClassUtils.getValue(nameField, null);
		return value;
	}

	/** Helper method of {@link #opString(ModuleInfo, ModuleItem)}. */
	private static String paramString(final Iterable<ModuleItem<?>> items,
		final ModuleItem<?> special)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final ModuleItem<?> item : items) {
			if (first) first = false;
			else sb.append(",");
			sb.append("\n");
			if (item == special) sb.append("==>"); // highlight special item
			sb.append("\t\t");
			sb.append(item.getType().getSimpleName() + " " + item.getName());
			if (!item.isRequired()) sb.append("?");
		}
		return sb.toString();
	}

}
