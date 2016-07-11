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

import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.special.computer.NullaryComputerOp;
import net.imagej.ops.special.function.NullaryFunctionOp;
import net.imagej.ops.special.hybrid.NullaryHybridCF;

import org.scijava.type.Nil;

/**
 * A <em>nullary</em> operation computes a result in a vacuum, without any input
 * values.
 * <p>
 * Nullary ops come in two major flavors: {@link NullaryComputerOp} and
 * {@link NullaryFunctionOp}. An additional hybrid type {@link NullaryHybridCF}
 * unions both flavors.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <O> type of output
 */
public interface NullaryOp<O> extends SpecialOp, Output<O> {

	/**
	 * Executes the operation in a type-safe but flexible way.
	 * <p>
	 * The exact behavior depends on the type of special op.
	 * </p>
	 * @param output reference where the operation's result will be stored
	 * @return result of the operation
	 * @see NullaryComputerOp#run(Object)
	 * @see NullaryFunctionOp#run(Object)
	 * @see NullaryHybridCF#run(Object)
	 */
	O run(O output);

	// -- SpecialOp methods --

	@Override
	default int getArity() {
		return 0;
	}

	// -- Runnable methods --

	@Override
	default void run() {
		run(out());
	}

	// -- Threadable methods --

	@Override
	default NullaryOp<O> getIndependentInstance() {
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
	 *          {@link NullaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link NullaryOp}s implement),
	 *          then the best {@link NullaryOp} implementation to use will
	 *          be selected automatically from the type and arguments.
	 * @param otherArgs The operation's arguments, excluding the typed output
	 *          value.
	 * @return A {@link NullaryOp} with populated inputs, ready to use.
	 */
	static <O, OP extends NullaryOp<O>> OP op(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final Object... otherArgs)
	{
		return opO(ops, opType, specialType, null, otherArgs);
	}

	/**
	 * Gets the best {@link NullaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link NullaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link NullaryOp}s implement),
	 *          then the best {@link NullaryOp} implementation to use will
	 *          be selected automatically from the type and arguments.
	 * @param out The typed output.
	 * @param otherArgs The operation's arguments, excluding the typed output
	 *          value.
	 * @return A {@link NullaryOp} with populated inputs, ready to use.
	 */
	static <O, OP extends NullaryOp<O>> OP opO(final OpEnvironment ops,
		final Class<? extends Op> opType, final Nil<OP> specialType, final O out,
		final Object... otherArgs)
	{
		final Object[] args = SpecialOp.args(specialType, otherArgs, out);
		return SpecialOp.op(ops, opType, specialType, args);
	}

}
