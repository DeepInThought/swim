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

import {AnyLength, Length} from "@swim/length";
import {AnyColor, Color} from "@swim/color";
import {MemberAnimator} from "../member/MemberAnimator";
import {RenderedViewInit, RenderedView} from "./RenderedView";

export interface StrokeViewInit extends RenderedViewInit {
  stroke?: AnyColor;
  strokeWidth?: AnyLength;
}

export interface StrokeView extends RenderedView {
  readonly stroke: MemberAnimator<this, Color, AnyColor>;

  readonly strokeWidth: MemberAnimator<this, Length, AnyLength>;
}

/** @hidden */
export const StrokeView = {
  is(object: unknown): object is StrokeView {
    if (typeof object === "object" && object !== null) {
      const view = object as StrokeView;
      return RenderedView.is(view)
          && "stroke" in view
          && "strokeWidth" in view;;
    }
    return false;
  },
};
