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

import {Murmur3} from "@swim/util";
import {Output} from "@swim/codec";
import {LengthUnits, Length} from "./Length";

export class PxLength extends Length {
  /** @hidden */
  readonly _value: number;
  /** @hidden */
  readonly _node?: Node;

  constructor(value: number, node: Node | null = null) {
    super();
    this._value = value;
    if (node !== null) {
      this._node = node;
    }
  }

  isRelative(): boolean {
    return false;
  }

  get value(): number {
    return this._value;
  }

  get units(): LengthUnits {
    return "px";
  }

  get node(): Node | null {
    const node = this._node;
    return node !== void 0 ? node : null;
  }

  unitValue(): number {
    return 1;
  }

  pxValue(): number {
    return this._value;
  }

  px(): PxLength {
    return this;
  }

  equals(that: unknown): boolean {
    if (that instanceof PxLength) {
      return this._value === that._value && this._node === that._node;
    }
    return false;
  }

  hashCode(): number {
    if (PxLength._hashSeed === void 0) {
      PxLength._hashSeed = Murmur3.seed(PxLength);
    }
    return Murmur3.mash(Murmur3.mix(PxLength._hashSeed, Murmur3.hash(this._value)));
  }

  debug(output: Output): void {
    output = output.write("Length").write(46/*'.'*/).write("px").write(40/*'('*/).debug(this._value);
    if (this._node !== void 0) {
      output = output.write(", ").debug(this._node);
    }
    output = output.write(41/*')'*/);
  }

  toString(): string {
    return this._value + "px";
  }

  private static _hashSeed?: number;

  private static _zero: PxLength;
  static zero(units?: "px", node?: Node | null): PxLength;
  static zero(node?: Node | null): PxLength;
  static zero(units?: "px" | Node | null, node?: Node | null): PxLength {
    if (typeof units !== "string") {
      node = units;
      units = "px";
    }
    if (node === void 0 || node === null) {
      if (PxLength._zero === void 0) {
        PxLength._zero = new PxLength(0);
      }
      return PxLength._zero;
    } else {
      return new PxLength(0, node);
    }
  }
}
Length.Px = PxLength;
