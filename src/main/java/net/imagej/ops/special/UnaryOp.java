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
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.UnaryHybridCF;
import net.imagej.ops.special.hybrid.UnaryHybridCFI;
import net.imagej.ops.special.hybrid.UnaryHybridCI;
import net.imagej.ops.special.inplace.UnaryInplaceOp;

import org.scijava.type.Nil;

/**
 * A <em>unary</em> operation computes a result from a given input. The contents
 * of the input are not affected.
 * <p>
 * Unary ops come in three major flavors: {@link UnaryComputerOp},
 * {@link UnaryFunctionOp} and {@link UnaryInplaceOp}. Additional hybrid types
 * union these flavors in various combinations: {@link UnaryHybridCF},
 * {@link UnaryHybridCI} and {@link UnaryHybridCFI}.
 * </p>
 * <p>
 * A unary op may be treated as a {@link NullaryOp} by holding the input
 * constant.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <I> type of input
 * @param <O> type of output
 */
public interface UnaryOp<I, O> extends NullaryOp<O>, UnaryInput<I> {

	/**
	 * Executes the operation in a type-safe but flexible way.
	 * <p>
	 * The exact behavior depends on the type of special op.
	 * </p>
	 * @param input argument to the operation
	 * @param output reference where the operation's result will be stored
	 * @return result of the operation
	 * @see UnaryComputerOp#run(Object, Object)
	 * @see UnaryFunctionOp#run(Object, Object)
	 * @see UnaryInplaceOp#run(Object, Object)
	 * @see UnaryHybridCF#run(Object, Object)
	 */
	O run(I input, O output);

	// -- SpecialOp methods --

	@Override
	default int getArity() {
		return 1;
	}

	// -- Runnable methods --

	@Override
	default void run() {
		run(in(), out());
	}

	// -- Threadable methods --

	@Override
	default UnaryOp<I, O> getIndependentInstance() {
		// NB: We assume the op instance is thread-safe by default.
		// Individual implementations can override this assumption if they
		// have state (such as buffers) that cannot be shared across threads.
		return this;
	}

	// -- Utility methods --

	/**
	 * Gets the best {@link UnaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link UnaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link UnaryOp}s implement), then
	 *          the best {@link UnaryOp} implementation to use will be
	 *          selected automatically from the type and arguments.
	 * @param otherArgs The operation's arguments, excluding the typed input and
	 *          output values.
	 * @return A {@link UnaryOp} with populated inputs, ready to use.
	 */
	static <I, O, OP extends UnaryOp<I, O>> OP op(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final Object... otherArgs)
	{
		return opOI(ops, opType, specialType, null, null, otherArgs);
	}

	/**
	 * Gets the best {@link UnaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link UnaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link UnaryOp}s implement), then
	 *          the best {@link UnaryOp} implementation to use will be
	 *          selected automatically from the type and arguments.
	 * @param in The typed input.
	 * @param otherArgs The operation's arguments, excluding the typed input and
	 *          output values.
	 * @return A {@link UnaryOp} with populated inputs, ready to use.
	 */
	static <I, O, OP extends UnaryOp<I, O>> OP opI(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final I in, final Object... otherArgs)
	{
		return opOI(ops, opType, specialType, null, in, otherArgs);
	}

	/**
	 * Gets the best {@link UnaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link UnaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link UnaryOp}s implement), then
	 *          the best {@link UnaryOp} implementation to use will be
	 *          selected automatically from the type and arguments.
	 * @param out The typed output.
	 * @param in The typed input.
	 * @param otherArgs The operation's arguments, excluding the typed input and
	 *          output values.
	 * @return A {@link UnaryOp} with populated inputs, ready to use.
	 */
	static <I, O, OP extends UnaryOp<I, O>> OP opOI(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final O out, final I in,
		final Object... otherArgs)
	{
		final Object inArg = in == null ? Nil.of(inType(specialType)) : in;
		final Object[] args = SpecialOp.args(specialType, otherArgs, out, inArg);
		return SpecialOp.op(ops, opType, specialType, args);
	}

	static Type inType(final Nil<?> nil) {
		return SpecialOp.typeParam(nil.getType(), UnaryInput.class, 0);
	}

}
