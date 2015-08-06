/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.idea.editors.gfxtrace.service.atom;

import org.jetbrains.annotations.NotNull;

import com.android.tools.rpclib.binary.BinaryClass;
import com.android.tools.rpclib.binary.BinaryID;
import com.android.tools.rpclib.binary.BinaryObject;
import com.android.tools.rpclib.binary.Decoder;
import com.android.tools.rpclib.binary.Encoder;
import com.android.tools.rpclib.binary.Namespace;

import java.io.IOException;

public final class Observations implements BinaryObject {
  //<<<Start:Java.ClassBody:1>>>
  Observation[] myReads;
  Observation[] myWrites;

  // Constructs a default-initialized {@link Observations}.
  public Observations() {}


  public Observation[] getReads() {
    return myReads;
  }

  public Observations setReads(Observation[] v) {
    myReads = v;
    return this;
  }

  public Observation[] getWrites() {
    return myWrites;
  }

  public Observations setWrites(Observation[] v) {
    myWrites = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }

  private static final byte[] IDBytes = {97, -33, -86, 18, 79, 83, 26, 84, -110, 78, -112, -60, 5, 124, -12, 95, 0, -53, 98, -23, };
  public static final BinaryID ID = new BinaryID(IDBytes);

  static {
    Namespace.register(ID, Klass.INSTANCE);
  }
  public static void register() {}
  //<<<End:Java.ClassBody:1>>>
  public enum Klass implements BinaryClass {
    //<<<Start:Java.KlassBody:2>>>
    INSTANCE;

    @Override @NotNull
    public BinaryID id() { return ID; }

    @Override @NotNull
    public BinaryObject create() { return new Observations(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      Observations o = (Observations)obj;
      e.uint32(o.myReads.length);
      for (int i = 0; i < o.myReads.length; i++) {
        e.value(o.myReads[i]);
      }
      e.uint32(o.myWrites.length);
      for (int i = 0; i < o.myWrites.length; i++) {
        e.value(o.myWrites[i]);
      }
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      Observations o = (Observations)obj;
      o.myReads = new Observation[d.uint32()];
      for (int i = 0; i <o.myReads.length; i++) {
        o.myReads[i] = new Observation();
        d.value(o.myReads[i]);
      }
      o.myWrites = new Observation[d.uint32()];
      for (int i = 0; i <o.myWrites.length; i++) {
        o.myWrites[i] = new Observation();
        d.value(o.myWrites[i]);
      }
    }
    //<<<End:Java.KlassBody:2>>>
  }
}
