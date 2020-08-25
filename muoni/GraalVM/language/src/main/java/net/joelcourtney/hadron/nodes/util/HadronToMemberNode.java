/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.joelcourtney.hadron.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import net.joelcourtney.hadron.nodes.HadronTypes;
import net.joelcourtney.hadron.runtime.HadronBigNumber;

/**
 * The node to normalize any value to an SL value. This is useful to reduce the number of values
 * expression nodes need to expect.
 */
@TypeSystemReference(HadronTypes.class)
@GenerateUncached
public abstract class HadronToMemberNode extends Node {

    static final int LIMIT = 5;

    public abstract String execute(Object value) throws UnknownIdentifierException;

    @Specialization
    protected static String fromString(String value) {
        return value;
    }

    @Specialization
    protected static String fromBoolean(boolean value) {
        return String.valueOf(value);
    }

    @Specialization
    @TruffleBoundary
    protected static String fromLong(long value) {
        return String.valueOf(value);
    }

    @Specialization
    @TruffleBoundary
    protected static String fromBigNumber(HadronBigNumber value) {
        return value.toString();
    }

    @Specialization(limit = "LIMIT")
    protected static String fromInterop(Object value, @CachedLibrary("value") InteropLibrary interop) throws UnknownIdentifierException {
        try {
            if (interop.fitsInLong(value)) {
                return longToString(interop.asLong(value));
            } else if (interop.isString(value)) {
                return interop.asString(value);
            } else if (interop.isNumber(value) && value instanceof HadronBigNumber) {
                return bigNumberToString((HadronBigNumber) value);
            } else {
                throw error(value);
            }
        } catch (UnsupportedMessageException e) {
            CompilerDirectives.transferToInterpreter();
            throw new AssertionError();
        }
    }

    @TruffleBoundary
    private static UnknownIdentifierException error(Object value) {
        return UnknownIdentifierException.create(value.toString());
    }

    @TruffleBoundary
    private static String bigNumberToString(HadronBigNumber value) {
        return value.toString();
    }

    @TruffleBoundary
    private static String longToString(long longValue) {
        return String.valueOf(longValue);
    }

}
