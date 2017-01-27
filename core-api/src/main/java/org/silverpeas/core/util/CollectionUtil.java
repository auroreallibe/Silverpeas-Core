/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import java.util.*;

/**
 * @author Yohann Chastagnier
 */
public class CollectionUtil {

  /**
   * Reverse the given list and returns it.
   * @param <T> the type of the instance contained into the specified list
   * @param list the list to reverse
   * @return the specified list that has been reversed (same instance)
   */
  public static <T> List<T> reverse(List<T> list) {
    Collections.reverse(list);
    return list;
  }

  /**
   * Checks if the given collection is not instanced or empty
   * @param <T> the type of the instance contained into the specified collection
   * @param collection the collection to verify
   * @return true if specified collection is empty, false otherwise
   */
  public static <T> boolean isEmpty(final Collection<T> collection) {
    return !isNotEmpty(collection);
  }

  /**
   * Checks if the given collection is instanced and not empty
   * @param <T> the type of the instance contained into the specified collection
   * @param collection the collection to verify
   * @return true if specified collection is not empty, false otherwise
   */
  public static <T> boolean isNotEmpty(final Collection<T> collection) {
    return collection != null && !collection.isEmpty();
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param collection the collection to split
   * @return the clices of the specified collection
   */
  public static <T> Collection<Collection<T>> split(final Collection<T> collection) {
    return split(collection, 500);
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param collection the collection to split
   * @param collectionSizeMax the maximum elements in slice
   * @return the clices of the specified collection
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<Collection<T>> split(final Collection<T> collection,
      final int collectionSizeMax) {
    Collection<Collection<T>> result = null;

    try {
      if (isNotEmpty(collection)) {
        if (collectionSizeMax > 0 && collection.size() > collectionSizeMax) {

          // Guessing the result size and initializing the result
          int size = (collection.size() / collectionSizeMax);
          if ((collection.size() % collectionSizeMax) != 0) {
            size++;
          }
          result = new ArrayList<>(size);

          // Browsing the collection
          Collection<T> curLot = null;
          for (final T element : collection) {

            // If necessary, initializing a lot
            if (curLot == null || curLot.size() >= collectionSizeMax) {
              curLot = new ArrayList<>(collectionSizeMax);

              // Adding the new lot
              result.add(curLot);
            }

            // Adding an element into the current lot
            curLot.add(element);
          }
        } else {
          result = Collections.singletonList(collection);
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (result == null) {
        result = new ArrayList<>();
      }
    }

    // Returning the result
    return result;
  }

  @SafeVarargs
  public static <T> List<T> asList(T... values) {
    List<T> listWithValues = new ArrayList<>();
    Collections.addAll(listWithValues, values);
    return listWithValues;
  }

  @SafeVarargs
  public static <T> Set<T> asSet(T... values) {
    return new HashSet<>(Arrays.asList(values));
  }

  /**
   * Null elements are not taking into account.
   * @see Collections#addAll(java.util.Collection, Object[])
   */
  @SafeVarargs
  public static <T> boolean addAllIgnoreNull(Collection<? super T> c, T... elements) {
    boolean result = false;
    for (T element : elements) {
      if (element != null) {
        result |= c.add(element);
      }
    }
    return result;
  }

  /**
   * Makes an union between both of the given lists.<br/>
   * The result contains unique values.
   * @param list1 the first list.
   * @param list2 the second list.
   * @param <T>
   * @return the union between the two lists.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> union(List<T> list1, List<T> list2) {
    return new ArrayList<T>(union((Collection) list1, (Collection) list2));
  }

  /**
   * Makes an union between both of the given collections.<br/>
   * The result contains unique values.
   * @param col1 the first collection.
   * @param col2 the second collection.
   * @param <T>
   * @return the union between the two collections.
   */
  public static <T> Collection<T> union(Collection<T> col1, Collection<T> col2) {
    Set<T> set = new LinkedHashSet<T>();
    set.addAll(col1);
    set.addAll(col2);
    return set;
  }

  /**
   * Makes an intersection between both of the given lists.<br/>
   * The result contains unique values.
   * @param list1 the first list.
   * @param list2 the second list.
   * @param <T>
   * @return the intersection between the two lists.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> intersection(List<T> list1, List<T> list2) {
    return (List) intersection((Collection) list1, (Collection) list2);
  }

  /**
   * Makes an intersection between both of the given collections.<br/>
   * The result contains unique values.
   * @param col1 the first collection.
   * @param col2 the second collection.
   * @param <T>
   * @return the intersection between the two collections.
   */
  public static <T> Collection<T> intersection(Collection<T> col1, Collection<T> col2) {
    List<T> list = new ArrayList<T>(new LinkedHashSet<T>(col1));
    Iterator<T> iterator = list.iterator();
    while(iterator.hasNext()) {
      if (!col2.contains(iterator.next())) {
        iterator.remove();
      }
    }
    return list;
  }

  /**
   * Transforming a collection into a map
   * @param <T> collection type elements
   * @param <K> map type key
   * @param <V> map type value
   * @param collection the collection to map
   * @param extractor extractor interface
   * @return a map initialized from a list by an extractor
   */
  public static <T, K, V> HashMap<K, V> listToMap(final Collection<T> collection,
      final ExtractionList<T, K, V> extractor) {
    final LinkedHashMap<K, V> result;
    if (collection == null) {
      result = null;
    } else if (collection.isEmpty()) {
      result = new LinkedHashMap<>();
    } else {
      result = new LinkedHashMap<>((int) (collection.size() * 0.75f));
      if (extractor instanceof ExtractionComplexList<?, ?, ?>) {
        ((ExtractionComplexList<T, K, V>) extractor).setMap(result);
      }
      for (final T toPerform : collection) {
        result.put(extractor.getKey(toPerform), extractor.getValue(toPerform));
      }
    }
    return result;
  }

  private interface ExtractionList<I, C, W> {

    C getKey(I in);

    W getValue(I in);
  }

  private abstract class ExtractionComplexList<I, C, W> implements ExtractionList<I, C, W> {

    /**
     * The result map
     */
    private Map<C, W> map;

    /**
     * @return the map
     */
    protected final Map<C, W> getMap() {
      return map;
    }

    /**
     * @param map the map to set
     */
    protected final void setMap(final Map<C, W> map) {
      this.map = map;
    }
  }
}
