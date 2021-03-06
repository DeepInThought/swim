// Copyright 2015-2020 SWIM.AI inc.
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

import {PointR2, BoxR2} from "@swim/math";
import {AnyLength, Length} from "@swim/length";
import {AnyColor, Color} from "@swim/color";
import {AnyFont, Font} from "@swim/font";
import {Tween} from "@swim/transition";
import {CanvasContext, CanvasRenderer} from "@swim/render";
import {
  View,
  MemberAnimator,
  RenderedView,
  FillViewInit,
  FillView,
  StrokeViewInit,
  StrokeView,
} from "@swim/view";
import {GeoPoint} from "../geo/GeoPoint";
import {GeoBox} from "../geo/GeoBox";
import {MapViewContext} from "../MapViewContext";
import {MapViewInit} from "../MapView";
import {MapGraphicsView} from "../graphics/MapGraphicsView";
import {MapGraphicsViewController} from "../graphics/MapGraphicsViewController";
import {AnyMapPointView, MapPointView} from "./MapPointView";

export type AnyMapPolygonView = MapPolygonView | MapPolygonViewInit;

export interface MapPolygonViewInit extends MapViewInit, FillViewInit, StrokeViewInit {
  points?: AnyMapPointView[];

  clipViewport?: true;

  font?: AnyFont;
  textColor?: AnyColor;
}

export class MapPolygonView extends MapGraphicsView implements FillView, StrokeView {
  /** @hidden */
  _geoCenter: GeoPoint;
  /** @hidden */
  _viewCenter: PointR2;
  /** @hidden */
  _clipViewport: boolean;
  /** @hidden */
  _viewBounds: BoxR2;
  /** @hidden */
  _geoBounds: GeoBox;

  constructor() {
    super();
    this._geoCenter = GeoPoint.origin();
    this._viewCenter = PointR2.origin();
    this._clipViewport = true;
    this._viewBounds = BoxR2.empty();
    this._geoBounds = GeoBox.empty();
  }

  get viewController(): MapGraphicsViewController<MapPolygonView> | null {
    return this._viewController;
  }

  points(): ReadonlyArray<MapPointView>;
  points(points: AnyMapPointView[], tween?: Tween<GeoPoint>): this;
  points(points?: AnyMapPointView[], tween?: Tween<GeoPoint>): ReadonlyArray<MapPointView> | this {
    const childViews = this._childViews;
    if (points === void 0) {
      points = [];
      for (let i = 0; i < childViews.length; i += 1) {
        const childView = childViews[i];
        if (childView instanceof MapPointView) {
          points.push(childView);
        }
      }
      return points as ReadonlyArray<MapPointView>;
    } else {
      const oldGeoBounds = this._geoBounds;
      let lngMin = Infinity;
      let latMin = Infinity;
      let lngMax = -Infinity;
      let latMax = -Infinity;
      let lngMid = 0;
      let latMid = 0;
      let invalid = false;
      let i = 0;
      let j = 0;
      while (i < childViews.length && j < points.length) {
        const childView = childViews[i];
        if (childView instanceof MapPointView) {
          const point = points[j];
          childView.setState(point);
          const {lng, lat} = childView.geoPoint.value!;
          lngMid += lng;
          latMid += lat;
          lngMin = Math.min(lngMin, lng);
          latMin = Math.min(latMin, lat);
          lngMax = Math.max(lng, lngMax);
          latMax = Math.max(lat, latMax);
          invalid = invalid || !isFinite(lng) || !isFinite(lat);
          j += 1;
        }
        i += 1;
      }
      while (j < points.length) {
        const point = MapPointView.fromAny(points[j]);
        this.appendChildView(point);
        const {lng, lat} = point.geoPoint.value!;
        lngMid += lng;
        latMid += lat;
        lngMin = Math.min(lngMin, lng);
        latMin = Math.min(latMin, lat);
        lngMax = Math.max(lng, lngMax);
        latMax = Math.max(lat, latMax);
        invalid = invalid || !isFinite(lng) || !isFinite(lat);
        i += 1;
        j += 1;
      }
      while (i < childViews.length) {
        const childView = childViews[i];
        if (childView instanceof MapPointView) {
          this.removeChildView(childView);
        } else {
          i += 1;
        }
      }
      if (!invalid && j !== 0) {
        lngMid /= j;
        latMid /= j;
        this._geoCenter = new GeoPoint(lngMid, latMid);
        this._geoBounds = new GeoBox(lngMin, latMin, lngMax, latMax);
      } else {
        this._geoCenter = GeoPoint.origin();
        this._geoBounds = GeoBox.empty();
      }
      const newGeoBounds = this._geoBounds;
      if (!oldGeoBounds.equals(newGeoBounds)) {
        this.didSetGeoBounds(newGeoBounds, oldGeoBounds);
      }
      return this;
    }
  }

  appendPoint(point: AnyMapPointView, key?: string): MapPointView {
    point = MapPointView.fromAny(point);
    this.appendChildView(point, key);
    return point;
  }

  setPoint(key: string, point: AnyMapPointView): MapPointView {
    point = MapPointView.fromAny(point);
    this.setChildView(key, point);
    return point;
  }

  clipViewport(): boolean;
  clipViewport(clipViewport: boolean): this;
  clipViewport(clipViewport?: boolean): boolean | this {
    if (clipViewport === void 0) {
      return this._clipViewport;
    } else {
      this._clipViewport = clipViewport;
      return this;
    }
  }

  get geoCenter(): GeoPoint {
    return this._geoCenter;
  }

  get viewCenter(): PointR2 {
    return this._viewCenter;
  }

  @MemberAnimator(Color, {inherit: true})
  fill: MemberAnimator<this, Color, AnyColor>;

  @MemberAnimator(Color, {inherit: true})
  stroke: MemberAnimator<this, Color, AnyColor>;

  @MemberAnimator(Length, {inherit: true})
  strokeWidth: MemberAnimator<this, Length, AnyLength>;

  @MemberAnimator(Font, {inherit: true})
  font: MemberAnimator<this, Font, AnyFont>;

  @MemberAnimator(Color, {inherit: true})
  textColor: MemberAnimator<this, Color, AnyColor>;

  protected onInsertChildView(childView: View, targetView: View | null | undefined): void {
    super.onInsertChildView(childView, targetView);
    if (childView instanceof MapPointView) {
      this.onInsertPoint(childView);
    }
  }

  protected onInsertPoint(childView: MapPointView): void {
    childView.requireUpdate(View.NeedsAnimate | View.NeedsProject);
  }

  protected didProject(viewContext: MapViewContext): void {
    const oldGeoBounds = this._geoBounds;
    let lngMin = Infinity;
    let latMin = Infinity;
    let lngMax = -Infinity;
    let latMax = -Infinity;
    let lngMid = 0;
    let latMid = 0;
    let xMin = Infinity;
    let yMin = Infinity;
    let xMax = -Infinity;
    let yMax = -Infinity;
    let xMid = 0;
    let yMid = 0;
    let invalid = false;
    let pointCount = 0;
    const childViews = this._childViews;
    for (let i = 0; i < childViews.length; i += 1) {
      const childView = childViews[i];
      if (childView instanceof MapPointView) {
        const {lng, lat} = childView.geoPoint.value!;
        lngMid += lng;
        latMid += lat;
        lngMin = Math.min(lngMin, lng);
        latMin = Math.min(latMin, lat);
        lngMax = Math.max(lng, lngMax);
        latMax = Math.max(lat, latMax);
        invalid = invalid || !isFinite(lng) || !isFinite(lat);
        const {x, y} = childView.viewPoint.value!;
        xMin = Math.min(xMin, x);
        yMin = Math.min(yMin, y);
        xMax = Math.max(x, xMax);
        yMax = Math.max(y, yMax);
        xMid += x;
        yMid += x;
        invalid = invalid || !isFinite(x) || !isFinite(y);
        pointCount += 1;
      }
    }
    if (!invalid && pointCount !== 0) {
      lngMid /= pointCount;
      latMid /= pointCount;
      this._geoCenter = new GeoPoint(lngMid, latMid);
      this._geoBounds = new GeoBox(lngMin, latMin, lngMax, latMax);
      xMid /= pointCount;
      yMid /= pointCount;
      this._viewCenter = new PointR2(xMid, yMid);
      this._viewBounds = new BoxR2(xMin, yMin, xMax, yMax);
      if (viewContext.geoFrame.intersects(this._geoBounds)) {
        const frame = this.viewFrame;
        const bounds = this._viewBounds;
        // check if 9x9 view frame fully contains view bounds
        const contained = !this._clipViewport
                       || frame.xMin - 4 * frame.width <= bounds.xMin
                       && bounds.xMax <= frame.xMax + 4 * frame.width
                       && frame.yMin - 4 * frame.height <= bounds.yMin
                       && bounds.yMax <= frame.yMax + 4 * frame.height;
        const culled = !contained || !frame.intersects(bounds);
        this.setCulled(culled);
      } else {
        this.setCulled(true);
      }
    } else {
      this._geoCenter = GeoPoint.origin();
      this._geoBounds = GeoBox.empty();
      this._viewCenter = PointR2.origin();
      this._viewBounds = BoxR2.empty();
      this.setCulled(true);
    }
    const newGeoBounds = this._geoBounds;
    if (!oldGeoBounds.equals(newGeoBounds)) {
      this.didSetGeoBounds(newGeoBounds, oldGeoBounds);
    }
    super.didProject(viewContext);
  }

  protected onRender(viewContext: MapViewContext): void {
    super.onRender(viewContext);
    const renderer = viewContext.renderer;
    if (renderer instanceof CanvasRenderer && !this.isHidden() && !this.isCulled()) {
      const context = renderer.context;
      context.save();
      this.renderPolygon(context, this.viewFrame);
      context.restore();
    }
  }

  protected renderPolygon(context: CanvasContext, frame: BoxR2): void {
    const childViews = this._childViews;
    const childCount = childViews.length;
    let pointCount = 0;
    context.beginPath();
    for (let i = 0; i < childCount; i += 1) {
      const childView = childViews[i];
      if (childView instanceof MapPointView) {
        const {x, y} = childView.viewPoint.value!;
        if (pointCount === 0) {
          context.moveTo(x, y);
        } else {
          context.lineTo(x, y);
        }
        pointCount += 1;
      }
    }
    context.closePath();
    if (pointCount !== 0) {
      const fill = this.fill.value;
      if (fill !== void 0) {
        context.fillStyle = fill.toString();
        context.fill();
      }
      const stroke = this.stroke.value;
      const strokeWidth = this.strokeWidth.value;
      if (stroke !== void 0 && strokeWidth !== void 0) {
        const size = Math.min(frame.width, frame.height);
        context.lineWidth = strokeWidth.pxValue(size);
        context.strokeStyle = stroke.toString();
        context.stroke();
      }
    }
  }

  get popoverFrame(): BoxR2 {
    const viewCenter = this._viewCenter;
    const inversePageTransform = this.pageTransform.inverse();
    const [px, py] = inversePageTransform.transform(viewCenter.x, viewCenter.y);
    return new BoxR2(px, py, px, py);
  }

  get viewBounds(): BoxR2 {
    return this._viewBounds;
  }

  get hitBounds(): BoxR2 {
    return this.viewBounds;
  }

  get geoBounds(): GeoBox {
    return this._geoBounds;
  }

  hitTest(x: number, y: number, viewContext: MapViewContext): RenderedView | null {
    let hit = super.hitTest(x, y, viewContext);
    if (hit === null) {
      const renderer = viewContext.renderer;
      if (renderer instanceof CanvasRenderer) {
        const context = renderer.context;
        context.save();
        x *= renderer.pixelRatio;
        y *= renderer.pixelRatio;
        hit = this.hitTestPolygon(x, y, context, this.viewFrame);
        context.restore();
      }
    }
    return hit;
  }

  protected hitTestPolygon(x: number, y: number, context: CanvasContext, frame: BoxR2): RenderedView | null {
    const childViews = this._childViews;
    const childCount = childViews.length;
    let pointCount = 0;
    context.beginPath();
    for (let i = 0; i < childCount; i += 1) {
      const childView = this._childViews[i];
      if (childView instanceof MapPointView) {
        const {x, y} = childView.viewPoint.value!;
        if (i === 0) {
          context.moveTo(x, y);
        } else {
          context.lineTo(x, y);
        }
        pointCount += 1;
      }
    }
    context.closePath();
    if (pointCount !== 0) {
      if (this.fill.value !== void 0 && context.isPointInPath(x, y)) {
        return this;
      }
      if (this.stroke.value !== void 0) {
        const strokeWidth = this.strokeWidth.value;
        if (strokeWidth !== void 0) {
          const size = Math.min(frame.width, frame.height);
          context.lineWidth = strokeWidth.pxValue(size);
          if (context.isPointInStroke(x, y)) {
            return this;
          }
        }
      }
    }
    return null;
  }

  static fromAny(polygon: AnyMapPolygonView): MapPolygonView {
    if (polygon instanceof MapPolygonView) {
      return polygon;
    } else if (typeof polygon === "object" && polygon !== null) {
      const view = new MapPolygonView();
      if (polygon.clipViewport !== void 0) {
        view.clipViewport(polygon.clipViewport);
      }
      if (polygon.fill !== void 0) {
        view.fill(polygon.fill);
      }
      if (polygon.stroke !== void 0) {
        view.stroke(polygon.stroke);
      }
      if (polygon.strokeWidth !== void 0) {
        view.strokeWidth(polygon.strokeWidth);
      }
      if (polygon.font !== void 0) {
        view.font(polygon.font);
      }
      if (polygon.textColor !== void 0) {
        view.textColor(polygon.textColor);
      }
      const points = polygon.points;
      if (points !== void 0) {
        view.points(points);
      }
      if (polygon.hidden !== void 0) {
        view.setHidden(polygon.hidden);
      }
      if (polygon.culled !== void 0) {
        view.setCulled(polygon.culled);
      }
      return view;
    }
    throw new TypeError("" + polygon);
  }
}
