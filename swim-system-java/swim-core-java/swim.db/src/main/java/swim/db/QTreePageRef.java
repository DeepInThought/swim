// Copyright 2015-2019 SWIM.AI inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package swim.db;

import java.lang.ref.WeakReference;
import swim.codec.Output;
import swim.codec.Unicode;
import swim.concurrent.Cont;
import swim.concurrent.Conts;
import swim.concurrent.Sync;
import swim.recon.Recon;
import swim.structure.Num;
import swim.structure.Record;
import swim.structure.Slot;
import swim.structure.Value;
import swim.util.CombinerFunction;
import swim.util.Cursor;

public final class QTreePageRef extends PageRef {
  final PageContext context;
  final PageType pageType;
  final int stem;
  final int post;
  final int zone;
  final long base;
  final long span;
  final long x;
  final long y;
  final Value fold;
  Object page;
  int pageRefSize;
  int pageSize;
  int diffSize;
  long treeSize;

  public QTreePageRef(PageContext context, PageType pageType, int stem, int post,
                      int zone, long base, long span, long x, long y, Value fold,
                      Object page, int pageRefSize, int pageSize, int diffSize, long treeSize) {
    this.context = context;
    this.pageType = pageType;
    this.stem = stem;
    this.post = post;
    this.zone = zone;
    this.base = base;
    this.span = span;
    this.x = x;
    this.y = y;
    this.fold = fold;
    this.page = page;
    this.pageRefSize = pageRefSize;
    this.pageSize = pageSize;
    this.diffSize = diffSize;
    this.treeSize = treeSize;
  }

  public QTreePageRef(PageContext context, PageType pageType, int stem, int post,
                      int zone, long base, long span, long x, long y, Value fold, Object page) {
    this(context, pageType, stem, post, zone, base, span, x, y, fold, page, -1, -1, -1, -1L);
  }

  public QTreePageRef(PageContext context, PageType pageType, int stem, int post,
                      int zone, long base, long span, long x, long y, Value fold) {
    this(context, pageType, stem, post, zone, base, span, x, y, fold, null, -1, -1, -1, -1L);
  }

  @Override
  public PageContext pageContext() {
    return this.context;
  }

  @Override
  public PageType pageType() {
    return this.pageType;
  }

  @Override
  public int stem() {
    return this.stem;
  }

  @Override
  public int post() {
    return this.post;
  }

  @Override
  public int zone() {
    return this.zone;
  }

  @Override
  public long base() {
    return this.base;
  }

  @Override
  public long span() {
    return this.span;
  }

  public long x() {
    return this.x;
  }

  public int xRank() {
    return Long.numberOfLeadingZeros(~this.x);
  }

  public long xBase() {
    return this.x << xRank();
  }

  public long xMask() {
    return ~((1L << xRank()) - 1L);
  }

  public long xSplit() {
    return this.x << 1 & 1L;
  }

  public long y() {
    return this.y;
  }

  public int yRank() {
    return Long.numberOfLeadingZeros(~this.y);
  }

  public long yBase() {
    return this.y << yRank();
  }

  public long yMask() {
    return ~((1L << yRank()) - 1L);
  }

  public long ySplit() {
    return this.y << 1 & 1L;
  }

  @Override
  public Value fold() {
    return this.fold;
  }

  @Override
  public QTreePage page() {
    Object page = this.page;
    if (page instanceof WeakReference<?>) {
      page = ((WeakReference<?>) page).get();
    }
    if (page instanceof QTreePage) {
      this.context.hitPage((QTreePage) page);
      return (QTreePage) page;
    } else {
      try (PageLoader pageLoader = this.context.openPageLoader(false)) {
        final Sync<Page> syncPage = new Sync<Page>();
        pageLoader.loadPageAsync(this, syncPage);
        return (QTreePage) syncPage.await(settings().pageLoadTimeout);
      } catch (InterruptedException error) {
        throw new StoreException(toDebugString(), error);
      } catch (Throwable error) {
        if (Conts.isNonFatal(error)) {
          throw new StoreException(toDebugString(), error);
        } else {
          throw error;
        }
      }
    }
  }

  @Override
  public QTreePage hardPage() {
    final Object page = this.page;
    if (page instanceof QTreePage) {
      return (QTreePage) page;
    } else {
      return null;
    }
  }

  @Override
  public QTreePage softPage() {
    Object page = this.page;
    if (page instanceof WeakReference<?>) {
      page = ((WeakReference<?>) page).get();
    }
    if (page instanceof QTreePage) {
      return (QTreePage) page;
    } else {
      return null;
    }
  }

  @Override
  public long softVersion() {
    final QTreePage page = softPage();
    if (page != null) {
      return page.version();
    } else {
      return 0L;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.span == 0L;
  }

  @Override
  public boolean isCommitted() {
    return this.zone > 0 && this.base > 0L;
  }

  @Override
  public int pageRefSize() {
    int pageRefSize = this.pageRefSize;
    if (pageRefSize < 0) {
      pageRefSize = 5; // "@page"
      if (this.post != this.zone) {
        pageRefSize += 6; // "(post:"
        pageRefSize += Recon.sizeOf(Num.from(this.post));
      }
      pageRefSize += 6; // "[(,]zone:"
      pageRefSize += Recon.sizeOf(Num.from(this.zone));
      pageRefSize += 6; // ",base:"
      pageRefSize += Recon.sizeOf(Num.from(this.base));
      pageRefSize += 6; // ",size:"
      pageRefSize += Recon.sizeOf(Num.from(pageSize()));
      pageRefSize += 6; // ",area:"
      pageRefSize += Recon.sizeOf(Num.from(treeSize()));
      pageRefSize += 6; // ",span:"
      pageRefSize += Recon.sizeOf(Num.from(this.span));
      pageRefSize += 3; // ",x:"
      pageRefSize += 18; // "0xXXXXXXXXXXXXXXXX";
      pageRefSize += 3; // ",y:"
      pageRefSize += 18; // "0xXXXXXXXXXXXXXXXX";
      final Value fold = fold();
      if (fold.isDefined()) {
        pageRefSize += 6; // ",fold:"
        pageRefSize += Recon.sizeOf(fold);
      }
      pageRefSize += 1; // ')'
      this.pageRefSize = pageRefSize; // Must match bytes written by writePageRef
    }
    return pageRefSize;
  }

  @Override
  public int pageSize() {
    if (this.pageSize < 0) {
      page().memoizeSize(this);
    }
    return this.pageSize;
  }

  @Override
  public int diffSize() {
    if (this.diffSize < 0) {
      page().memoizeSize(this);
    }
    return this.diffSize;
  }

  @Override
  public long treeSize() {
    if (this.treeSize < 0L) {
      page().memoizeSize(this);
    }
    return this.treeSize;
  }

  @Override
  public Value toValue() {
    final Record header = Record.create(9);
    if (this.post != this.zone) {
      header.slot("post", this.post);
    }
    header.slot("zone", this.zone)
          .slot("base", this.base)
          .slot("size", pageSize())
          .slot("area", treeSize())
          .slot("span", this.span)
          .slot("x", Num.uint64(this.x))
          .slot("y", Num.uint64(this.y));
    final Value fold = fold();
    if (fold.isDefined()) {
      header.slot("fold", fold);
    }
    return Record.create(1).attr(this.pageType.tag(), header);
  }

  public QTreePageRef reduced(Value identity, CombinerFunction<? super Value, Value> accumulator,
                              CombinerFunction<Value, Value> combiner, long newVersion) {
    if (!this.fold.isDefined()) {
      return page().reduced(identity, accumulator, combiner, newVersion).pageRef();
    } else {
      return this;
    }
  }

  @Override
  public QTreePageRef evacuated(int post, long version) {
    if (this.post != 0 && this.post < post) {
      return page().evacuated(post, version).pageRef();
    } else {
      return this;
    }
  }

  @Override
  public QTreePageRef committed(int zone, long base, long version) {
    final QTreePage page = hardPage();
    if (page != null) {
      return page.committed(zone, base, version).pageRef();
    } else {
      return this;
    }
  }

  @Override
  public QTreePageRef uncommitted(long version) {
    final QTreePage page = hardPage();
    if (page != null && page.version() >= version) {
      return page.uncommitted(version).pageRef();
    } else {
      return this;
    }
  }

  @Override
  public void writePageRef(Output<?> output) {
    Recon.write(toValue(), output);
  }

  @Override
  public void writePage(Output<?> output) {
    page().writePage(output);
  }

  @Override
  public void writeDiff(Output<?> output) {
    page().writeDiff(output);
  }

  @Override
  public QTreePage setPageValue(Value value, boolean isResident) {
    final QTreePage page = QTreePage.fromValue(this, value);
    if (isResident) {
      this.page = page;
    } else {
      this.context.hitPage(page);
      this.page = new WeakReference<Object>(page);
    }
    return page;
  }

  @Override
  public void loadPageAsync(boolean isResident, Cont<Page> cont) {
    try {
      Object page = this.page;
      if (page instanceof WeakReference<?>) {
        page = ((WeakReference<?>) page).get();
      }
      if (page instanceof QTreePage) {
        // Call continuation on fresh stack
        this.context.stage().execute(Conts.async(cont, (QTreePage) page));
      } else {
        final PageLoader pageLoader = this.context.openPageLoader(isResident);
        pageLoader.loadPageAsync(this, new LoadPage(pageLoader, cont));
      }
    } catch (Throwable cause) {
      if (Conts.isNonFatal(cause)) {
        cont.trap(new StoreException(toDebugString(), cause));
      } else {
        throw cause;
      }
    }
  }

  @Override
  public void loadTreeAsync(boolean isResident, Cont<Page> cont) {
    try {
      final PageLoader pageLoader = this.context.openPageLoader(isResident);
      loadTreeAsync(pageLoader, new LoadPage(pageLoader, cont));
    } catch (Throwable cause) {
      if (Conts.isNonFatal(cause)) {
        cont.trap(new StoreException(toDebugString(), cause));
      } else {
        throw cause;
      }
    }
  }

  @Override
  public void loadTreeAsync(PageLoader pageLoader, Cont<Page> cont) {
    try {
      Object page = this.page;
      if (page instanceof WeakReference<?>) {
        page = ((WeakReference<?>) page).get();
      }
      if (page instanceof QTreePage) {
        final Cont<Page> andThen = Conts.constant(cont, (QTreePage) page);
        ((QTreePage) page).loadTreeAsync(pageLoader, andThen);
      } else {
        pageLoader.loadPageAsync(this, new LoadTree(pageLoader, cont));
      }
    } catch (Throwable cause) {
      if (Conts.isNonFatal(cause)) {
        cont.trap(new StoreException(toDebugString(), cause));
      } else {
        throw cause;
      }
    }
  }

  @Override
  public void soften(long version) {
    final Object page = this.page;
    if (page instanceof QTreePage) {
      if (((QTreePage) page).version() <= version && isCommitted()) {
        this.context.hitPage((QTreePage) page);
        this.page = new WeakReference<Object>(page);
      }
      ((QTreePage) page).soften(version);
    }
  }

  @Override
  public Cursor<Slot> cursor() {
    return page().cursor();
  }

  public Cursor<Slot> cursor(long x, long y) {
    return page().cursor(x, y);
  }

  public Cursor<Slot> depthCursor(long x, long y, int maxDepth) {
    if (maxDepth > 0) {
      return page().depthCursor(x, y, maxDepth);
    } else {
      final Value fold = fold();
      if (fold instanceof Record) {
        return new QTreePageRefCursor(this, (Record) fold);
      } else if (fold.isDefined()) {
        return new QTreePageRefCursor(this, Record.of(fold));
      } else {
        return new QTreePageRefCursor(this, Record.empty());
      }
    }
  }

  public Cursor<Slot> depthCursor(int maxDepth) {
    if (maxDepth > 0) {
      return page().depthCursor(maxDepth);
    } else {
      final Value fold = fold();
      if (fold instanceof Record) {
        return new QTreePageRefCursor(this, (Record) fold);
      } else if (fold.isDefined()) {
        return new QTreePageRefCursor(this, Record.of(fold));
      } else {
        return new QTreePageRefCursor(this, Record.empty());
      }
    }
  }

  public Cursor<Slot> deltaCursor(long x, long y, long sinceVersion) {
    return page().deltaCursor(x, y, sinceVersion);
  }

  public Cursor<Slot> deltaCursor(long sinceVersion) {
    return page().deltaCursor(sinceVersion);
  }

  public Cursor<Slot> tileCursor(long x, long y) {
    return page().tileCursor(x, y);
  }

  public Cursor<Slot> tileCursor() {
    return page().tileCursor();
  }

  @Override
  public String toString() {
    final Output<String> output = Unicode.stringOutput(pageRefSize());
    writePageRef(output);
    return output.bind();
  }

  public static QTreePageRef empty(PageContext context, int stem, long version) {
    return QTreeLeaf.empty(context, stem, version).pageRef();
  }

  public static QTreePageRef fromValue(PageContext context, int stem, Value value) {
    Throwable cause = null;
    try {
      final String tag = value.tag();
      final PageType pageType = PageType.fromTag(tag);
      if (pageType == null) {
        return null;
      }
      final Value header = value.header(tag);
      final int zone = header.get("zone").intValue();
      final int post = header.get("post").intValue(zone);
      final long base = header.get("base").longValue();
      final int size = header.get("size").intValue();
      final long area = header.get("area").longValue();
      final long span = header.get("span").longValue();
      final long x = header.get("x").longValue();
      final long y = header.get("y").longValue();
      final Value fold = header.get("fold");
      if (base < 0L) {
        throw new StoreException("negative page base: " + base);
      } else if (size < 0) {
        throw new StoreException("negative page size: " + size);
      } else if (area < 0) {
        throw new StoreException("negative page area: " + area);
      } else if (span < 0) {
        throw new StoreException("negative page span: " + span);
      }
      return new QTreePageRef(context, pageType, stem, post, zone, base, span,
                              x, y, fold, null, -1, size, 0, area);
    } catch (Throwable error) {
      if (Conts.isNonFatal(error)) {
        cause = error;
      } else {
        throw error;
      }
    }
    final Output<String> message = Unicode.stringOutput("Malformed qtree page ref: ");
    Recon.write(value, message);
    throw new StoreException(message.bind(), cause);
  }
}
