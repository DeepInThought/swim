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

import {FromAny} from "@swim/util";
import {Tween, Transition} from "@swim/transition";
import {MemberAnimatorInherit, MemberAnimator} from "./MemberAnimator";
import {AnimatedView} from "../AnimatedView";

/** @hidden */
export class AnyMemberAnimator<V extends AnimatedView, T, U = T> extends MemberAnimator<V, T, T | U> {
  /** @hidden */
  readonly _type: FromAny<T, U>;

  constructor(type: FromAny<T, U>, view: V, value?: T | null, transition?: Transition<T> | null, inherit?: MemberAnimatorInherit) {
    super(view, value, transition, inherit);
    this._type = type;
    let animator = this;
    function accessor(): T | undefined;
    function accessor(value: T | U | null, tween?: Tween<T>): V;
    function accessor(value?: T | U | null, tween?: Tween<T>): T | null | undefined | V {
      if (value === void 0) {
        return animator.value;
      } else {
        if (value !== null) {
          value = animator.type.fromAny(value);
        }
        animator.setState(value, tween);
        return animator._view;
      }
    }
    (accessor as any).__proto__ = animator;
    animator = accessor as any;
    return animator;
  }

  get type(): FromAny<T, U> {
    return this._type;
  }
}
MemberAnimator.Any = AnyMemberAnimator;
