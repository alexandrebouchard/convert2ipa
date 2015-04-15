package ventris;

import java.util.*;
import java.io.*;

public interface Metric<T>
{
  double d(T n1, T n2);
}
