/*
 * Copyright 2023 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dorkbox.serializers;

import java.lang.reflect.Field;

public
class UnmodifiableCollectionJavaAccessor {
    public static final Field SOURCE_MAP_FIELD = null;

    // the class methods here are rewritten using javassist.
    public static Object UnmodifiableCollection_Field(Object nativeUnmodifiableCollection) {
        return null;
    }

    public static void initUnmodifiableMap_Field() {
    }

    public static Object UnmodifiableMap_Field(Object nativeUnmodifiableMap) {
        return null;
    }
}
