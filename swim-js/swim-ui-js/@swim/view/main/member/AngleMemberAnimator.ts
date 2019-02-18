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

import {AnyAngle, Angle} from "@swim/angle";
import {Tween, Transition} from "@swim/transition";
import {MemberAnimatorInherit, MemberAnimator} from "./MemberAnimator";
import {AnimatedView} from "../AnimatedView";

/** @hidden */
export class AngleMemberAnimator<V extends AnimatedView> extends MemberAnimator<V, Angle, AnyAngle> {
  constructor(view: V, value?: Angle | null, transition?: Transition<Angle> | null, inherit?: MemberAnimatorInherit) {
    super(view, value, transition, inherit);
    let animator = this;
    function accessor(): Angle | undefined;
    function accessor(value: AnyAngle | null, tween?: Tween<Angle>): V;
    function accessor(value?: AnyAngle | null, tween?: Tween<Angle>): Angle | null | undefined | V {
      if (value === void 0) {
        return animator.value;
      } else {
        if (value !== null) {
          value = Angle.fromAny(value);
        }
        animator.setState(value, tween);
        return animator._view;
      }
    }
    (accessor as any).__proto__ = animator;
    animator = accessor as any;
    return animator;
  }
}
MemberAnimator.Angle = AngleMemberAnimator;
