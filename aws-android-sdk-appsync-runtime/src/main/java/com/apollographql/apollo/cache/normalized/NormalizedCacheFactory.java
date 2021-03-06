/**
 * Copyright 2018-2018 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *     http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, express or implied. See the License
 * for the specific language governing permissions and
 * limitations under the License.
 */

package com.apollographql.apollo.cache.normalized;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.CustomTypeAdapter;
import com.apollographql.apollo.api.ScalarType;
import com.apollographql.apollo.api.internal.Function;
import com.apollographql.apollo.api.internal.Optional;

import javax.annotation.Nonnull;

import static com.apollographql.apollo.api.internal.Utils.checkNotNull;

/**
 * A Factory used to construct an instance of a {@link NormalizedCache} configured with the custom scalar adapters set
 * in {@link ApolloClient.Builder#addCustomTypeAdapter(ScalarType, CustomTypeAdapter)}.
 */
public abstract class NormalizedCacheFactory<T extends NormalizedCache> {
  private Optional<NormalizedCacheFactory> nextFactory = Optional.absent();

  /**
   * @param recordFieldAdapter A {@link RecordFieldJsonAdapter} configured with the custom scalar adapters set in {@link
   *                           ApolloClient.Builder#addCustomTypeAdapter(ScalarType,
   *                           CustomTypeAdapter)}.
   * @return An implementation of {@link NormalizedCache}.
   */
  public abstract T create(RecordFieldJsonAdapter recordFieldAdapter);

  public final NormalizedCache createChain(final RecordFieldJsonAdapter recordFieldAdapter) {
    if (nextFactory.isPresent()) {
      return create(recordFieldAdapter)
          .chain(nextFactory.map(new Function<NormalizedCacheFactory, NormalizedCache>() {
            @Nonnull @Override public NormalizedCache apply(@Nonnull NormalizedCacheFactory factory) {
              return factory.createChain(recordFieldAdapter);
            }
          }).get());
    } else {
      return create(recordFieldAdapter);
    }
  }

  public final NormalizedCacheFactory<T> chain(@Nonnull NormalizedCacheFactory factory) {
    checkNotNull(factory, "factory == null");

    NormalizedCacheFactory leafFactory = this;
    while (leafFactory.nextFactory.isPresent()) {
      leafFactory = (NormalizedCacheFactory) leafFactory.nextFactory.get();
    }
    leafFactory.nextFactory = Optional.of(factory);

    return this;
  }
}
