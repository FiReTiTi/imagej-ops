/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
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

package net.imagej.ops.special;

import java.lang.reflect.Type;

import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;

import org.scijava.type.Nil;
import org.scijava.type.Types;

/**
 * Utility class for looking up {@link SpecialOp}s in a type-safe way.
 *
 * @author Curtis Rueden
 */
public final class Specials {

	private Specials() {
		// NB: Prevent instantiation of utility class.
	}

	// FIXME RENAME all these methods to suck less.

	/**
	 * Gets the best {@link UnaryOp} implementation for the given types and
	 * arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link NullaryOp}s share this type (e.g., the type is an interface
	 *          which multiple {@link NullaryOp}s implement), then the best
	 *          {@link NullaryOp} implementation to use will be selected
	 *          automatically from the type and arguments.
	 * @param otherArgs The operation's arguments, excluding any typed input
	 *          and/or output values.
	 * @return A {@link SpecialOp} with populated inputs, ready to use.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <OP extends SpecialOp> OP op(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final Object... otherArgs)
	{
		final Type t = specialType.getType();
		if (Types.isAssignable(t, NullaryOp.class)) {
			return (OP) NullaryOp.op(ops, opType, (Nil) specialType, otherArgs);
		}
		if (Types.isAssignable(t, UnaryOp.class)) {
			return (OP) UnaryOp.op(ops, opType, (Nil) specialType, otherArgs);
		}
		if (Types.isAssignable(t, BinaryOp.class)) {
			return (OP) BinaryOp.op(ops, opType, (Nil) specialType, otherArgs);
		}
		throw new IllegalArgumentException("Unknown arity: " + specialType);
	}

	// -- Helper methods --

	// TODO Move these? If so, where? OpUtils? Can we rename that class maybe?

}
