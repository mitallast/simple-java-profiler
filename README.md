simple-java-profiler
====================

Simple java profiler based on java-agent and javassist

Run `mvn clean package` and add java agent to your application:

Example of usage:

```sh
java -javaagent:target/agent-1.0-SNAPSHOT.jar Hello.class
...
 - top [0] method [org.apache.lucene.store.ByteBufferIndexInput#readByte] invocations [57010618]
 - top [1] method [org.apache.lucene.util.packed.GrowableWriter#get] invocations [41373611]
 - top [2] method [org.apache.lucene.store.DataInput#readVInt] invocations [21140370]
 - top [3] method [org.apache.lucene.store.DataInput#readInt] invocations [7002954]
 - top [4] method [org.apache.lucene.store.IndexInput#toString] invocations [3338825]
 - top [5] method [org.elasticsearch.common.collect.AbstractIndexedListIterator#hasNext] invocations [68070]
 - top [6] method [org.elasticsearch.common.collect.ImmutableEntry#getKey] invocations [45685]
 - top [7] method [org.elasticsearch.common.collect.Hashing#smear] invocations [40337]
 - top [8] method [org.elasticsearch.common.collect.RegularImmutableMap#checkNoConflictInBucket] invocations [20790]
 - top [9] method [org.elasticsearch.common.collect.ImmutableMap#checkNoConflict] invocations [9270]
 - top [0] class [org.apache.lucene.util.packed.GrowableWriter] invocations [82771873]
 - top [1] class [org.apache.lucene.store.DataInput] invocations [31786116]
 - top [2] class [org.apache.lucene.store.IndexInput] invocations [3338828]
 - top [3] class [org.elasticsearch.common.collect.AbstractIndexedListIterator] invocations [100738]
 - top [4] class [org.elasticsearch.common.base.Preconditions] invocations [74657]
 - top [5] class [org.elasticsearch.common.collect.RegularImmutableMap] invocations [31827]
 - top [6] class [org.elasticsearch.common.collect.ImmutableMap] invocations [15066]
 - top [7] class [org.elasticsearch.common.settings.ImmutableSettings] invocations [9448]
 - top [8] class [org.elasticsearch.common.settings.ImmutableSettings$Builder] invocations [1540]
 - top [9] class [org.elasticsearch.bootstrap.Bootstrap] invocations [7]
```
