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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.Op;
import net.imagej.ops.deconvolve.RichardsonLucyUpdate;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.junit.Test;
import org.scijava.type.Nil;
import org.scijava.type.Types;
import org.scijava.util.ClassUtils;
import org.scijava.util.GenericUtils;

/**
 * Tests that the new opX method works.
 *
 * @author Curtis Rueden
 */
public class SpecialsTest<I extends RealType<I>, O extends RealType<O>> extends AbstractOpTest {

	private UnaryComputerOp<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> update;

	/*
	C1: out, in
	C1: out, in, foo?, bar?

	F1: in
	F1: in, foo?, bar?

	HC*1: out?, in
	HC*1: out?, in, foo?, bar?
		- Cases: 1) leave off out+in; 2) leave off out, specify in; 3) specify all

	IO1: arg

	Output means: out -- BUT: not necessarily also an input -- how to tell?
	OutputMutable means: out as an _input_ -- might be optional, might not
	UnaryInput always means: in -- never optional
	BinaryInput always means: in1, in2 -- never optional
	*/

	@Test
	public void go() {
		UnaryComputerOp<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> op =
			Specials.unaryNils(ops, RichardsonLucyUpdate.class,
				new Nil<UnaryComputerOp<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>>>() {});
		assertSame(op.getClass(), RichardsonLucyUpdate.class);

		assertNull(update);
		fill("update", RichardsonLucyUpdate.class);
		assertSame(update.getClass(), RichardsonLucyUpdate.class);
	}
	
	// FIXME: Move this to OpEnvironment
	public void fill(final String field, final Class<? extends Op> opType, final Object... args)
	{
		// get the field
		final Class<?> c = getClass();
		final Class<?> base = c; // TODO: Walk superclasses recursively to find it.
		final Field f = ClassUtils.getField(base, field);
		if (f == null) {
			throw new IllegalArgumentException("No such field: " + field);
		}

		// extract its type
		final Type type = GenericUtils.getFieldType(f, c);
		System.out.println("field type = " + Types.name(type));

		// wrap it in a Nil
		final Nil<?> nil = Nil.of(type);

		// look up the op
		final Object op = SpecialOp.opX(ops, opType, nil, args);

		// inject result into the field
		f.setAccessible(true);
		try {
			f.set(this, op);
		}
		catch (final IllegalAccessException exc) {
			throw new IllegalArgumentException("Cannot write field: " + field, exc);
		}
	}

}
