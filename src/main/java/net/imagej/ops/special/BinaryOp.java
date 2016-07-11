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
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.function.BinaryFunctionOp;
import net.imagej.ops.special.hybrid.BinaryHybridCF;
import net.imagej.ops.special.hybrid.BinaryHybridCFI;
import net.imagej.ops.special.hybrid.BinaryHybridCFI1;
import net.imagej.ops.special.hybrid.BinaryHybridCI;
import net.imagej.ops.special.hybrid.BinaryHybridCI1;
import net.imagej.ops.special.inplace.BinaryInplaceOp;

import org.scijava.type.Nil;

/**
 * A <em>binary</em> operation computes a result from two given inputs. The
 * contents of the inputs are not affected.
 * <p>
 * Binary ops come in three major flavors: {@link BinaryComputerOp},
 * {@link BinaryFunctionOp} and {@link BinaryInplaceOp}. Additional hybrid types
 * union these flavors in various combinations: {@link BinaryHybridCF},
 * {@link BinaryHybridCI1}, {@link BinaryHybridCI}, {@link BinaryHybridCFI1} and
 * {@link BinaryHybridCFI}.
 * </p>
 * <p>
 * A binary op may be treated as a {@link UnaryOp} by holding the second input
 * constant, or treated as a {@link NullaryOp} by holding both inputs constant.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <I1> type of first input
 * @param <I2> type of second input
 * @param <O> type of output
 */
public interface BinaryOp<I1, I2, O> extends UnaryOp<I1, O>,
	BinaryInput<I1, I2>
{

	/**
	 * Executes the operation in a type-safe but flexible way.
	 * <p>
	 * The exact behavior depends on the type of special op.
	 * </p>
	 * @param input1 first argument to the operation
	 * @param input2 second argument to the operation
	 * @param output reference where the operation's result will be stored
	 * @return result of the operation
	 * @see BinaryComputerOp#run(Object, Object, Object)
	 * @see BinaryFunctionOp#run(Object, Object, Object)
	 * @see BinaryInplaceOp#run(Object, Object, Object)
	 * @see BinaryHybridCF#run(Object, Object, Object)
	 */
	O run(I1 input1, I2 input2, O output);

	// -- SpecialOp methods --

	@Override
	default int getArity() {
		return 2;
	}

	// -- Runnable methods --

	@Override
	default void run() {
		run(in1(), in2(), out());
	}

	// -- Threadable methods --

	@Override
	default BinaryOp<I1, I2, O> getIndependentInstance() {
		// NB: We assume the op instance is thread-safe by default.
		// Individual implementations can override this assumption if they
		// have state (such as buffers) that cannot be shared across threads.
		return this;
	}

	// -- Utility methods --

	/**
	 * Gets the best {@link BinaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link BinaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link BinaryOp}s implement),
	 *          then the best {@link BinaryOp} implementation to use will
	 *          be selected automatically from the type and arguments.
	 * @param otherArgs The operation's arguments, excluding the typed inputs and
	 *          output values.
	 * @return A {@link BinaryOp} with populated inputs, ready to use.
	 */
	public static <I1, I2, O, OP extends BinaryOp<I1, I2, O>> OP op(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final Object... otherArgs)
	{
		return opOI(ops, opType, specialType, null, null, null, otherArgs);
	}

	/**
	 * Gets the best {@link BinaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link BinaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link BinaryOp}s implement),
	 *          then the best {@link BinaryOp} implementation to use will
	 *          be selected automatically from the type and arguments.
	 * @param in1 The first typed input.
	 * @param in2 The second typed input.
	 * @param otherArgs The operation's arguments, excluding the typed inputs and
	 *          output values.
	 * @return A {@link BinaryOp} with populated inputs, ready to use.
	 */
	public static <I1, I2, O, OP extends BinaryOp<I1, I2, O>> OP opI(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final I1 in1, final I2 in2,
		final Object... otherArgs)
	{
		return opOI(ops, opType, specialType, null, in1, in2, otherArgs);
	}

	/**
	 * Gets the best {@link BinaryOp} implementation for the given types
	 * and arguments, populating its inputs.
	 *
	 * @param ops The {@link OpEnvironment} to search for a matching op.
	 * @param opType The {@link Class} of the operation. If multiple
	 *          {@link BinaryOp}s share this type (e.g., the type is an
	 *          interface which multiple {@link BinaryOp}s implement),
	 *          then the best {@link BinaryOp} implementation to use will
	 *          be selected automatically from the type and arguments.
	 * @param out The typed output.
	 * @param in1 The first typed input.
	 * @param in2 The second typed input.
	 * @param otherArgs The operation's arguments, excluding the typed inputs and
	 *          output values.
	 * @return A {@link BinaryOp} with populated inputs, ready to use.
	 */
	public static <I1, I2, O, OP extends BinaryOp<I1, I2, O>> OP opOI(
		final OpEnvironment ops, final Class<? extends Op> opType,
		final Nil<OP> specialType, final O out, final I1 in1, final I2 in2,
		final Object... otherArgs)
	{
		final Object in1Arg = in1 == null ? Nil.of(in1Type(specialType)) : in1;
		final Object in2Arg = in2 == null ? Nil.of(in2Type(specialType)) : in2;
		final Object[] args = SpecialOp.args(specialType, otherArgs, out, in1Arg,
			in2Arg);
		return SpecialOp.op(ops, opType, specialType, args);
	}

	static Type in1Type(final Nil<?> nil) {
		return SpecialOp.typeParam(nil.getType(), BinaryInput.class, 0);
	}

	static Type in2Type(final Nil<?> nil) {
		return SpecialOp.typeParam(nil.getType(), BinaryInput.class, 1);
	}

}
