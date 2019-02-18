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

import {Tween, Transition} from "@swim/transition";
import {AttributeAnimator} from "./AttributeAnimator";
import {ElementView} from "../ElementView";

/** @hidden */
export class NumberAttributeAnimator<V extends ElementView> extends AttributeAnimator<V, number, number | string> {
  constructor(target: V, name: string, value?: number | null, transition?: Transition<number> | null) {
    super(target, name, value, transition);
    let animator = this;
    function accessor(): number | null | undefined;
    function accessor(value: number | string | null, tween?: Tween<number>): V;
    function accessor(value?: number | string | null, tween?: Tween<number>): number | null | undefined | V {
      if (value === void 0) {
        return animator.value;
      } else {
        if (typeof value === "string") {
          value = +value;
        }
        animator.setState(value, tween);
        return animator._view;
      }
    }
    (accessor as any).__proto__ = animator;
    animator = accessor as any;
    return animator;
  }

  get value(): number | null | undefined {
    let value = this._value;
    if (value === void 0) {
      const attributeValue = this.attributeValue;
      if (attributeValue) {
        value = +attributeValue;
      }
    }
    return value;
  }
}
AttributeAnimator.Number = NumberAttributeAnimator;
