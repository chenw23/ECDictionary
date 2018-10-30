import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Node {
    private boolean isLeaf;
    private boolean isRoot;
    private Node parent;
    private Node previous;
    private Node next;
    private List<Entry<Comparable, Object>> entries;
    private List<Node> children;

    private Node(boolean isLeaf) {
        this.isLeaf = isLeaf;
        entries = new ArrayList<>();
        if (!isLeaf) {
            children = new ArrayList<>();
        }
    }

    Node(boolean isLeaf, boolean isRoot) {
        this(isLeaf);
        this.isRoot = isRoot;
    }

    Object get(Comparable key) {
        if (isLeaf) {
            for (Entry<Comparable, Object> entry : entries) {
                if (entry.getKey().compareTo(key) == 0) {
                    return entry.getValue();
                }
            }
            return null;
        } else {
            if (key.compareTo(entries.get(0).getKey()) <= 0) {
                return children.get(0).get(key);
            } else if (key.compareTo(entries.get(entries.size() - 1).getKey()) >= 0) {
                return children.get(children.size() - 1).get(key);
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i + 1).getKey().compareTo(key) > 0) {
                        return children.get(i).get(key);
                    }
                }
            }
        }
        return null;
    }

    void insertOrUpdate(Comparable key, Object obj, BPlusTree tree) {
        if (isLeaf) {
            if (contains(key) || entries.size() < tree.getOrder()) {
                insertOrUpdate(key, obj);
                if (parent != null) {
                    parent.updateInsert(tree);
                }
            } else {
                Node left = new Node(true);
                Node right = new Node(true);
                if (previous != null) {
                    previous.setNext(left);
                    left.setPrevious(previous);
                }
                if (next != null) {
                    next.setPrevious(right);
                    right.setNext(next);
                }
                left.setNext(right);
                right.setPrevious(left);
                previous = null;
                next = null;
                int leftSize = (tree.getOrder() + 1) / 2 + (tree.getOrder() + 1) % 2;
                int rightSize = (tree.getOrder() + 1) / 2;
                insertOrUpdate(key, obj);
                for (int i = 0; i < leftSize; i++)
                    left.getEntries().add(entries.get(i));
                for (int i = 0; i < rightSize; i++)
                    right.getEntries().add(entries.get(leftSize + i));
                if (parent != null) {
                    int index = parent.getChildren().indexOf(this);
                    parent.getChildren().remove(this);
                    left.setParent(parent);
                    right.setParent(parent);
                    parent.getChildren().add(index, left);
                    parent.getChildren().add(index + 1, right);
                    setEntries(null);
                    setChildren(null);
                    parent.updateInsert(tree);
                    setParent(null);
                } else {
                    isRoot = false;
                    Node parent = new Node(false, true);
                    tree.setRoot(parent);
                    left.setParent(parent);
                    right.setParent(parent);
                    parent.getChildren().add(left);
                    parent.getChildren().add(right);
                    setEntries(null);
                    setChildren(null);
                    parent.updateInsert(tree);
                }
            }
        } else {
            if (key.compareTo(entries.get(0).getKey()) <= 0) {
                children.get(0).insertOrUpdate(key, obj, tree);
            } else if (key.compareTo(entries.get(entries.size() - 1).getKey()) >= 0) {
                children.get(children.size() - 1).insertOrUpdate(key, obj, tree);
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i + 1).getKey().compareTo(key) > 0) {
                        children.get(i).insertOrUpdate(key, obj, tree);
                        break;
                    }
                }
            }
        }
    }


    private void updateInsert(BPlusTree tree) {
        validate(this, tree);
        if (children.size() > tree.getOrder()) {
            Node left = new Node(false);
            Node right = new Node(false);
            int leftSize = (tree.getOrder() + 1) / 2 + (tree.getOrder() + 1) % 2;
            int rightSize = (tree.getOrder() + 1) / 2;
            for (int i = 0; i < leftSize; i++) {
                left.getChildren().add(children.get(i));
                left.getEntries().add(new SimpleEntry(children.get(i).getEntries().get(0).getKey(), null));
                children.get(i).setParent(left);
            }
            for (int i = 0; i < rightSize; i++) {
                right.getChildren().add(children.get(leftSize + i));
                right.getEntries().add(new SimpleEntry(children.get(leftSize + i).getEntries().get(0).getKey(), null));
                children.get(leftSize + i).setParent(right);
            }
            if (parent != null) {
                int index = parent.getChildren().indexOf(this);
                parent.getChildren().remove(this);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildren().add(index, left);
                parent.getChildren().add(index + 1, right);
                setEntries(null);
                setChildren(null);
                parent.updateInsert(tree);
                setParent(null);
            } else {
                isRoot = false;
                Node parent = new Node(false, true);
                tree.setRoot(parent);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildren().add(left);
                parent.getChildren().add(right);
                setEntries(null);
                setChildren(null);
                parent.updateInsert(tree);
            }
        }
    }


    private static void validate(Node node, BPlusTree tree) {
        if (node.getEntries().size() == node.getChildren().size()) {
            for (int i = 0; i < node.getEntries().size(); i++) {
                Comparable key = node.getChildren().get(i).getEntries().get(0).getKey();
                if (node.getEntries().get(i).getKey().compareTo(key) != 0) {
                    node.getEntries().remove(i);
                    node.getEntries().add(i, new SimpleEntry(key, null));
                    if (!node.isRoot()) {
                        validate(node.getParent(), tree);
                    }
                }
            }
        } else if (node.isRoot() && node.getChildren().size() >= 2
                || node.getChildren().size() >= tree.getOrder() / 2
                && node.getChildren().size() <= tree.getOrder()
                && node.getChildren().size() >= 2) {
            node.getEntries().clear();
            for (int i = 0; i < node.getChildren().size(); i++) {
                Comparable key = node.getChildren().get(i).getEntries().get(0).getKey();
                node.getEntries().add(new SimpleEntry(key, null));
                if (!node.isRoot()) {
                    validate(node.getParent(), tree);
                }
            }
        }
    }


    private void updateRemove(BPlusTree tree) {
        validate(this, tree);
        if (children.size() < tree.getOrder() / 2 || children.size() < 2) {
            if (isRoot) {
                if (children.size() < 2) {
                    Node root = children.get(0);
                    tree.setRoot(root);
                    root.setParent(null);
                    root.setRoot(true);
                    setEntries(null);
                    setChildren(null);
                }
            } else {
                int currIdx = parent.getChildren().indexOf(this);
                int prevIdx = currIdx - 1;
                int nextIdx = currIdx + 1;
                Node previous = null, next = null;
                if (prevIdx >= 0) {
                    previous = parent.getChildren().get(prevIdx);
                }
                if (nextIdx < parent.getChildren().size()) {
                    next = parent.getChildren().get(nextIdx);
                }
                if (previous != null
                        && previous.getChildren().size() > tree.getOrder() / 2
                        && previous.getChildren().size() > 2) {
                    int idx = previous.getChildren().size() - 1;
                    Node borrow = previous.getChildren().get(idx);
                    previous.getChildren().remove(idx);
                    borrow.setParent(this);
                    children.add(0, borrow);
                    validate(previous, tree);
                    validate(this, tree);
                    parent.updateRemove(tree);
                } else if (next != null
                        && next.getChildren().size() > tree.getOrder() / 2
                        && next.getChildren().size() > 2) {
                    Node borrow = next.getChildren().get(0);
                    next.getChildren().remove(0);
                    borrow.setParent(this);
                    children.add(borrow);
                    validate(next, tree);
                    validate(this, tree);
                    parent.updateRemove(tree);
                } else {
                    if (previous != null
                            && (previous.getChildren().size() <= tree.getOrder() / 2 || previous.getChildren().size() <= 2)) {

                        for (int i = previous.getChildren().size() - 1; i >= 0; i--) {
                            Node child = previous.getChildren().get(i);
                            children.add(0, child);
                            child.setParent(this);
                        }
                        previous.setChildren(null);
                        previous.setEntries(null);
                        previous.setParent(null);
                        parent.getChildren().remove(previous);
                        validate(this, tree);
                        parent.updateRemove(tree);
                    } else if (next != null
                            && (next.getChildren().size() <= tree.getOrder() / 2 || next.getChildren().size() <= 2)) {

                        for (int i = 0; i < next.getChildren().size(); i++) {
                            Node child = next.getChildren().get(i);
                            children.add(child);
                            child.setParent(this);
                        }
                        next.setChildren(null);
                        next.setEntries(null);
                        next.setParent(null);
                        parent.getChildren().remove(next);
                        validate(this, tree);
                        parent.updateRemove(tree);
                    }
                }
            }
        }
    }

    void remove(Comparable key, BPlusTree tree) {
        if (isLeaf) {
            if (!contains(key))
                return;
            if (isRoot)
                remove(key);
             else {
                if (entries.size() > tree.getOrder() / 2 && entries.size() > 2) {
                    remove(key);
                } else {
                    if (previous != null
                            && previous.getEntries().size() > tree.getOrder() / 2
                            && previous.getEntries().size() > 2
                            && previous.getParent() == parent) {
                        int size = previous.getEntries().size();
                        Entry<Comparable, Object> entry = previous.getEntries().get(size - 1);
                        previous.getEntries().remove(entry);
                        entries.add(0, entry);
                        remove(key);
                    } else if (next != null
                            && next.getEntries().size() > tree.getOrder() / 2
                            && next.getEntries().size() > 2
                            && next.getParent() == parent) {
                        Entry<Comparable, Object> entry = next.getEntries().get(0);
                        next.getEntries().remove(entry);
                        entries.add(entry);
                        remove(key);
                    } else {
                        if (previous != null
                                && (previous.getEntries().size() <= tree.getOrder() / 2 || previous.getEntries().size() <= 2)
                                && previous.getParent() == parent) {
                            for (int i = previous.getEntries().size() - 1; i >= 0; i--) {
                                entries.add(0, previous.getEntries().get(i));
                            }
                            remove(key);
                            previous.setParent(null);
                            previous.setEntries(null);
                            parent.getChildren().remove(previous);
                            if (previous.getPrevious() != null) {
                                Node temp = previous;
                                temp.getPrevious().setNext(this);
                                previous = temp.getPrevious();
                                temp.setPrevious(null);
                                temp.setNext(null);
                            } else {
                                previous.setNext(null);
                                previous = null;
                            }
                        } else if (next != null
                                && (next.getEntries().size() <= tree.getOrder() / 2 || next.getEntries().size() <= 2)
                                && next.getParent() == parent) {
                            for (int i = 0; i < next.getEntries().size(); i++) {
                                entries.add(next.getEntries().get(i));
                            }
                            remove(key);
                            next.setParent(null);
                            next.setEntries(null);
                            parent.getChildren().remove(next);
                            if (next.getNext() != null) {
                                Node temp = next;
                                temp.getNext().setPrevious(this);
                                next = temp.getNext();
                                temp.setPrevious(null);
                                temp.setNext(null);
                            } else {
                                next.setPrevious(null);
                                next = null;
                            }
                        }
                    }
                }
                parent.updateRemove(tree);
            }
        } else {
            if (key.compareTo(entries.get(0).getKey()) <= 0) {
                children.get(0).remove(key, tree);
            } else if (key.compareTo(entries.get(entries.size() - 1).getKey()) >= 0) {
                children.get(children.size() - 1).remove(key, tree);
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i + 1).getKey().compareTo(key) > 0) {
                        children.get(i).remove(key, tree);
                        break;
                    }
                }
            }
        }
    }


    private boolean contains(Comparable key) {
        for (Entry<Comparable, Object> entry : entries) {
            if (entry.getKey().compareTo(key) == 0) {
                return true;
            }
        }
        return false;
    }


    private void insertOrUpdate(Comparable key, Object obj) {
        Entry<Comparable, Object> entry = new SimpleEntry<>(key, obj);
        if (entries.size() == 0) {
            entries.add(entry);
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getKey().compareTo(key) == 0) {
                entries.get(i).setValue(obj);
                return;
            } else if (entries.get(i).getKey().compareTo(key) > 0) {
                if (i == 0) {
                    entries.add(0, entry);
                    return;
                } else {
                    entries.add(i, entry);
                    return;
                }
            }
        }
        entries.add(entries.size(), entry);
    }

    private void remove(Comparable key) {
        int index = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getKey().compareTo(key) == 0) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            entries.remove(index);
        }
    }

    private Node getPrevious() {
        return previous;
    }

    private void setPrevious(Node previous) {
        this.previous = previous;
    }

    private Node getNext() {
        return next;
    }

    private void setNext(Node next) {
        this.next = next;
    }

    private Node getParent() {
        return parent;
    }

    private void setParent(Node parent) {
        this.parent = parent;
    }

    private List<Entry<Comparable, Object>> getEntries() {
        return entries;
    }

    private void setEntries(List<Entry<Comparable, Object>> entries) {
        this.entries = entries;
    }

    private List<Node> getChildren() {
        return children;
    }

    private void setChildren(List<Node> children) {
        this.children = children;
    }

    private boolean isRoot() {
        return isRoot;
    }

    private void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("isRoot: ");
        sb.append(isRoot);
        sb.append(", ");
        sb.append("isLeaf: ");
        sb.append(isLeaf);
        sb.append(", ");
        sb.append("keys: ");
        for (Entry entry : entries) {
            sb.append(entry.getKey());
            sb.append(", ");
        }
        sb.append(", ");
        return sb.toString();

    }
}