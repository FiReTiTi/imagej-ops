/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package org.scijava.type;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.FlatIterationOrder;
import net.imglib2.IterableInterval;
import net.imglib2.Point;
import net.imglib2.Positionable;
import net.imglib2.RandomAccessible;
import net.imglib2.RealPositionable;
import net.imglib2.img.NativeImg;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;

public class Tester {

	public static void main(String... args) {
//		Thing<FloatType, FloatArray> thing = new Thing<>(ArrayImgs.floats(2, 3));
//		ArrayImg<FloatType, FloatArray> thing = ArrayImgs.floats(2, 3);
//		List<String> thing = Collections.singletonList("Hi");
		Map<String, Integer> thing = Collections.singletonMap("Curtis", 37);

		Context ctx = new Context(TypeService.class);
		TypeService typeService = ctx.service(TypeService.class);

		Type goodType = typeService.typeOf(thing);
//		System.out.println("GOT IT: " + goodType);

		ctx.dispose();
	}


	/*
	we have a concrete type:
	Thing<U, B> implements IterableInterval<NativeImg<U, B>>, Stuff<Junk>

	And we define our object:
	Thing thing = new Thing<FloatType, FloatArray>();

	we want to extract the generic type at runtime:
	Thing<FloatType, FloatArray>

	(thing, [<U>, <B>])

	1) Get all direct generic supertypes of Thing. They are:
		IterableInterval<NativeImg<U, B>>,
		Stuff<Junk>

	The first one is worth analyzing because unfilled type vars are in there.

	We have an extractor for Iterable.
	However, we also need one for NativeImg for this to be useful.
	AND: the U and/or B appear in that nested type, so we analyze it.

	Iterable it = (Iterable) thing;
	Object element = it.iterator().next();

	(It is the case here that 'element' will be a subtype of NativeImg.)
	(Let's suppose it is, in practice here, an ArrayImg.)

	We ask the typeService for the type of 'element'.

	initial element is known to be:
	ArrayImg<T extends NativeType<T>, A> extends AbstractNativeImg< T, A >

	Looping over the supertypes, we discover:
		ArrayImg<T extends NativeType<T>, A> is an NativeImg<T, A>!

	There is an extractor for NativeType.
	AND: the T and/or A appear in that nested type, so we analyze it.

	NativeImg img = (NativeImg) element;
	Object a = img.update(null);
	Object t = img.firstElement();

	(It is the case here that 't' will be a subtype of NativeType.)
	(In practice here, as supposed above, it will be a FloatType.)
	(And again in practice, 'a' will be a FloatArray.)

	We ask the typeService for the type of 'a' and 't'.
	They are reified, so we get the classes.

	So, back to 'element', we construct and return the type as:
	ArrayImg<FloatType, FloatArray>

	And back to 'thing':
	--- START HERE: might want to change TypeExtractor to return only
			the types of the type variables, as a list?


/////// A 2nd EXAMPLE

	Thing<U, B> thing;
	
	Thing<A, B, C> implements RandomAccessible<NativeImg<U, B>>
	
	Thing is a NativeImg
	
	A=typeService.type(thing.update(null))
	B=typeService.type(thing.firstElement())
	C=?
	
	base extractor matches Thing
	
	could:
	- get all ifaces & classes of Thing
	- foreach type:
			if type has no variables, then continue
			so, type has variables
	*/
	
	
	// 

//		ParameterizedType p;
//		TypeVariable tv;
//		WildcardType wt;
//		java.lang.reflect.AnnotatedType
//		GenericUtils.

//		if (o instanceof Iterable) {
//			final Iterable<?> it = (Iterable<?>) o;
//			final TypeToken<?> superType =
//				TypeToken.of(it.getClass()).getSupertype(Iterable.class);
//			// supertype -> Iterable<NativeImg<U, B>>
//			// NativeImg<U, B> element = thing.firstElement();
//			// 
//			// typeService.typeOf(element);
//		}
////		GenericUtils.getTypeParameter(type, c, paramNo);
//		return o instanceof Iterable;
//	}

	// -- Helper classes --

	public interface Junk {}
	public interface Stuff<T> {}

	public static class Thing<U extends RealType<U>, B> extends Point implements
		IterableInterval<NativeImg<U, B>>, Stuff<Junk>
	{

		private NativeImg<U, B> img;

		public Thing(final NativeImg<U, B> img) {
			super(1);
			this.img = img;
		}

		@Override
		public long size() { return 1; }

		@Override
		public NativeImg<U, B> firstElement() {
			return img;
		}

		@Override
		public Object iterationOrder() {
			return new FlatIterationOrder( this );
		}

		@Override
		public double realMin(int d) {
			return 0;
		}

		@Override
		public void realMin(double[] min) {
			min[0] = 0;
		}

		@Override
		public void realMin(RealPositionable min) {
			min.setPosition(0, 0);
		}

		@Override
		public double realMax(int d) {
			return 0;
		}

		@Override
		public void realMax(double[] max) {
			max[0] = 0;
		}

		@Override
		public void realMax(RealPositionable max) {
			max.setPosition(0, 0);
		}

		@Override
		public Iterator<NativeImg<U, B>> iterator() {
			return cursor();
		}

		@Override
		public long min(int d) {
			return 0;
		}

		@Override
		public void min(long[] min) {
			min[0] = 0;
		}

		@Override
		public void min(Positionable min) {
			min.setPosition(0, 0);
		}

		@Override
		public long max(int d) {
			return 0;
		}

		@Override
		public void max(long[] max) {
			max[0] = 0;
		}

		@Override
		public void max(Positionable max) {
			max.setPosition(0, 0);
		}

		@Override
		public void dimensions(long[] dimensions) {
			dimensions[0] = 1;
		}

		@Override
		public long dimension(int d) {
			return 1;
		}

		@Override
		public Cursor<NativeImg<U, B>> cursor() {
			return new AbstractCursor<NativeImg<U, B>>(1) {

				@Override
				public NativeImg<U, B> get() {
					return img;
				}

				@Override
				public void fwd() {
					// TODO Auto-generated method stub
				}

				@Override
				public void reset() {
					// TODO Auto-generated method stub
				}

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public void localize(long[] pos) {
					// TODO Auto-generated method stub
				}

				@Override
				public long getLongPosition(int d) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public AbstractCursor<NativeImg<U, B>> copy() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public AbstractCursor<NativeImg<U, B>> copyCursor() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}

		@Override
		public Cursor<NativeImg<U, B>> localizingCursor() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	abstract static class Foo<A, B, C extends List<A> & RandomAccessible<B>>
		implements Set<Map<C, C>>
	{}
}
