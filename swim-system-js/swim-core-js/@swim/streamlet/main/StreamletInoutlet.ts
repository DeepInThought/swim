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

import {Streamlet} from "./Streamlet";
import {GenericStreamlet} from "./GenericStreamlet";
import {AbstractInoutlet} from "./AbstractInoutlet";

/**
 * An `Inoutlet` that decoheres a parameterized `Streamlet` whenever the
 * `Inoutlet` decoheres, that recoheres the parameterized `Streamlet`
 * whenever the `Inoutlet` recoheres, and which gets its state from the
 * parameterized `Streamlet`.
 */
export class StreamletInoutlet<I, O> extends AbstractInoutlet<I, O> {
  /** @hidden */
  protected readonly _streamlet: Streamlet<I, O>;

  constructor(streamlet: Streamlet<I, O>) {
    super();
    this._streamlet = streamlet;
  }

  streamlet(): Streamlet<I, O> {
    return this._streamlet;
  }

  get(): O | undefined {
    const streamlet = this._streamlet as GenericStreamlet<I, O>;
    if (streamlet.getOutput !== void 0) {
      const output = streamlet.getOutput(this);
      if (output !== void 0) {
        return output;
      }
    }
    if (this._input !== null) {
      return this._input.get() as O | undefined;
    }
    return void 0;
  }

  protected willDecohere(): void {
    const streamlet = this._streamlet as GenericStreamlet<I, O>;
    if (streamlet.willDecohereOutlet !== void 0) {
      streamlet.willDecohereOutlet(this);
    }
  }

  protected didDecohere(): void {
    const streamlet = this._streamlet as GenericStreamlet<I, O>;
    if (streamlet.didDecohereOutlet !== void 0) {
      streamlet.didDecohereOutlet(this);
    } else {
      streamlet.decohere();
    }
  }

  protected willRecohere(version: number): void {
    const streamlet = this._streamlet as GenericStreamlet<I, O>;
    if (streamlet.willRecohereOutlet !== void 0) {
      streamlet.willRecohereOutlet(this, version);
    }
  }

  protected didRecohere(version: number): void {
    const streamlet = this._streamlet as GenericStreamlet<I, O>;
    if (streamlet.didRecohereOutlet !== void 0) {
      streamlet.didRecohereOutlet(this, version);
    }
  }
}
