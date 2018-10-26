import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class RedBlackTree<K, V> extends StringTree<String, String> {
    private Entry<String, String> root;
    private EntrySet<Entry<String, String>> entrySet;
    private static final boolean RED = false;
    private static final boolean BLACK = true;

    RedBlackTree() {
    }

    @Override
    public String put(String key, String value) {
        Entry<String, String> t = root;
        if (t == null) {
            root = new Entry<>(key, value, null);
            return null;
        }
        int cmp;
        Entry<String, String> parent;
        if (key == null)
            throw new NullPointerException();
        do {
            parent = t;
            cmp = key.compareTo(t.getKey());
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                return t.setValue(value);
        } while (t != null);
        Entry<String, String> e = new Entry<>(key, value, parent);
        if (cmp < 0)
            parent.left = e;
        else
            parent.right = e;
        fixAfterInsertion(e);
        return null;
    }

    @Override
    public String get(Object object) {
        String key = object.toString();
        Entry<String, String> p = getEntry(key);
        return (p == null ? null : p.getValue());
    }

    @Contract("null -> fail")
    @Nullable
    private Entry<String, String> getEntry(String key) {
        if (key == null)
            throw new NullPointerException();
        Entry<String, String> p = root;
        while (p != null) {
            int cmp = key.compareTo(p.getKey());
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }

    public String remove(Object object) {
        String key = object.toString();
        Entry<String, String> p = getEntry(key);
        if (p == null)
            return null;
        String oldValue = p.getValue();
        deleteEntry(p);
        return oldValue;
    }

    private void deleteEntry(@NotNull Entry<String, String> p) {
        if (p.left != null && p.right != null) {
            Entry<String, String> s = successor(p);
            p.key = s.key;
            p.setValue(s.getValue());
            p = s;
        }
        Entry<String, String> replacement = (p.left != null ? p.left : p.right);
        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null)
                root = replacement;
            else if (p == p.parent.left)
                p.parent.left = replacement;
            else
                p.parent.right = replacement;
            p.left = p.right = p.parent = null;
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) {
            root = null;
        } else {
            if (p.color == BLACK)
                fixAfterDeletion(p);
            if (p.parent != null) {
                if (p == p.parent.left)
                    p.parent.left = null;
                else if (p == p.parent.right)
                    p.parent.right = null;
                p.parent = null;
            }
        }
    }

    @Contract(value = "null -> null", pure = true)
    private static Entry<String, String> successor(Entry<String, String> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            Entry<String, String> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            Entry<String, String> p = t.parent;
            Entry<String, String> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private void fixAfterInsertion(@NotNull Entry<String, String> x) {
        x.color = RED;
        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry<String, String> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Entry<String, String> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    private void fixAfterDeletion(Entry<String, String> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry<String, String> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK &&
                        colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else {
                Entry<String, String> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                        colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }
        setColor(x, BLACK);
    }

    void preOrderPrint() {
        if (root == null) {
            System.out.println("level=0 child=0 null");
            return;
        }
        String color;
        int level = 0;
        int child;
        Entry<String, String> currentNode = root;
        Entry<String, String> previousNode = null;

        while (true) {
            if (previousNode == null || previousNode == parentOf(currentNode)) {
                color = colorOf(currentNode) ? "BLACK" : "RED";
                child = (currentNode == rightOf(parentOf(currentNode))) ? 1 : 0;
                System.out.println("level=" + level + " child=" + child + " " +
                        currentNode.getKey() + "(" + color + ")");
                previousNode = currentNode;
                if (leftOf(currentNode) == null) {
                    System.out.println("level=" + (level + 1) + " child=0 null");
                    if (rightOf(currentNode) == null) {
                        System.out.println("level=" + (level + 1) + " child=1 null");
                        if (parentOf(currentNode) == null) break;
                        level--;
                        currentNode = currentNode.parent;
                    } else {
                        level++;
                        currentNode = currentNode.right;
                    }
                } else {
                    level++;
                    currentNode = currentNode.left;
                }
            } else if (previousNode == leftOf(currentNode)) {
                previousNode = currentNode;
                if (rightOf(currentNode) == null) {
                    System.out.println("level=" + (level + 1) + " child=1 null");
                    level--;
                    currentNode = currentNode.parent;
                } else {
                    level++;
                    currentNode = currentNode.right;
                }
            } else {
                if (currentNode == root) break;
                level--;
                previousNode = currentNode;
                currentNode = currentNode.parent;
            }
        }
    }

    @Contract(pure = true)
    private static boolean colorOf(Entry<String, String> p) {
        return (p == null ? BLACK : p.color);
    }

    @Contract(value = "null -> null", pure = true)
    private static Entry<String, String> parentOf(Entry<String, String> p) {
        return (p == null ? null : p.parent);
    }

    private static void setColor(Entry<String, String> p, boolean c) {
        if (p != null)
            p.color = c;
    }

    @Contract(value = "null -> null", pure = true)
    private static Entry<String, String> leftOf(Entry<String, String> p) {
        return (p == null) ? null : p.left;
    }

    @Contract(value = "null -> null", pure = true)
    private static Entry<String, String> rightOf(Entry<String, String> p) {
        return (p == null) ? null : p.right;
    }

    private void rotateLeft(Entry<String, String> p) {
        if (p != null) {
            Entry<String, String> r = p.right;
            p.right = r.left;
            if (r.left != null)
                r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null)
                root = r;
            else if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
            r.left = p;
            p.parent = r;
        }
    }

    private void rotateRight(Entry<String, String> p) {
        if (p != null) {
            Entry<String, String> l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null)
                root = l;
            else if (p.parent.right == p)
                p.parent.right = l;
            else p.parent.left = l;
            l.right = p;
            p.parent = l;
        }
    }

    @NotNull
    public Set<Map.Entry<String, String>> entrySet() {
        EntrySet es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet<Entry<String, String>>());
    }

    private static final class Entry<K, V> extends AbstractEntry<String, String> {
        String key;
        Entry<String, String> left;
        Entry<String, String> right;
        Entry<String, String> parent;
        boolean color = BLACK;

        Entry(String key, String value, Entry<String, String> parent) {
            super(key, value);
            this.key = key;
            this.parent = parent;
        }

        @Contract(pure = true)
        public String getKey() {
            return key;
        }
    }
}