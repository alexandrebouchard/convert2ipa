package ventris;

import java.util.HashSet;
import java.util.Set;

public class AgglomerativeClustering<T>
{
  private Metric<Cluster<T>> metric;
  
  public Cluster<T> cluster(Set<Cluster<T>> clusters)
  {
    while (clusters.size() > 1)
    {
      // compute the square distances and merge the 2 closest clusters
      double minDistance = Double.POSITIVE_INFINITY;
      Cluster<T> min1 = null;
      Cluster<T> min2 = null;
      for (Cluster<T> c1 : clusters)
      {
        for (Cluster<T> c2 : clusters)
        {
          double currentDistance = metric.d(c1, c2);
          if (currentDistance < minDistance)
          {
            minDistance = currentDistance;
            min1 = c1;
            min2 = c2;
          }
        }
      }
      clusters.remove(min1);
      clusters.remove(min2);
      Cluster<T> merged = Cluster.newInstanceFromSubClusters(min1, min2);
      clusters.add(merged);
    }
    return clusters.iterator().next();
  }

  public static class Cluster<V>
  {
    private Set<V> objects;
    private Cluster<V> subCluster1, subCluster2;
    private Cluster()
    {
    }
    public Set<V> getObjects()
    {
      return objects;
    }
    public Cluster<V> getSubCluster1()
    {
      return subCluster1;
    }
    public Cluster<V> getSubCluster2()
    {
      return subCluster2;
    }
    public static <V> Cluster<V> newInstanceFromObjects(Set<V> objects)
    {
      Cluster<V> newCluster = new Cluster<V>();
      newCluster.objects = objects;
      return newCluster;
    }
    public static <V> Cluster<V> newInstanceFromSubClusters(Cluster<V> cluster1, Cluster<V> cluster2)
    {
      Cluster<V> newCluster = new Cluster<V>();
      newCluster.subCluster1 = cluster1;
      newCluster.subCluster2 = cluster2;
      newCluster.objects = new HashSet<V>();
      newCluster.objects.addAll(cluster1.objects);
      newCluster.objects.addAll(cluster2.objects);
      return newCluster;
    }
    @Override
    public String toString()
    {
      // TODO : print the hierarchy
      return super.toString();
    }
  }
  
  public static class SingleLinkDistance<U> implements Metric<Cluster<U>>
  {
    private Metric<U> underlyingMetric;
    public SingleLinkDistance(Metric<U> underlyingMetric)
    {
      this.underlyingMetric = underlyingMetric;
    }
    public double d(Cluster<U> n1, Cluster<U> n2)
    {
      double min = Double.POSITIVE_INFINITY;
      for (U o1 : n1.objects)
      {
        for (U o2 : n2.objects)
        {
          double currentD = underlyingMetric.d(o1, o2);
          if (currentD < min)
          {
            min = currentD;
          }
        }
      }
      return min;
    }  
  }
  
  // TODO : complete link, average link, ward's method
  
  /*public static class PrecomputedDistance implements Metric<String>
  {
    private double [][] distances;
    // TODO : Load from a file the distance matrix and cache it
    public double d(String n1, String n2)
    {
      // TODO : Auto-generated method stub
      return 0;
    }
  }*/
  
  // TODO : main method
}
