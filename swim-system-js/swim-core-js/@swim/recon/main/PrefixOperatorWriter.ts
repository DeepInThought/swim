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

import {Output, WriterException, Writer, Unicode, Utf8} from "@swim/codec";
import {ReconWriter} from "./ReconWriter";

/** @hidden */
export class PrefixOperatorWriter<I, V> extends Writer {
  private readonly _recon: ReconWriter<I, V>;
  private readonly _operator: string;
  private readonly _rhs: I;
  private readonly _precedence: number;
  private readonly _part: Writer | undefined;
  private readonly _step: number | undefined;

  constructor(recon: ReconWriter<I, V>, operator: string, rhs: I,
              precedence: number, part?: Writer, step?: number) {
    super();
    this._recon = recon;
    this._operator = operator;
    this._rhs = rhs;
    this._precedence = precedence;
    this._part = part;
    this._step = step;
  }

  pull(output: Output): Writer {
    return PrefixOperatorWriter.write(output, this._recon, this._operator, this._rhs,
                                      this._precedence, this._part, this._step);
  }

  static sizeOf<I, V>(recon: ReconWriter<I, V>, operator: string, rhs: I, precedence: number): number {
    let size = 0;
    size += Utf8.sizeOf(operator);
    if (recon.precedence(rhs) < precedence) {
      size += 1; // '('
      size += recon.sizeOfItem(rhs);
      size += 1; // ')'
    } else {
      size += recon.sizeOfItem(rhs);
    }
    return size;
  }

  static write<I, V>(output: Output, recon: ReconWriter<I, V>, operator: string, rhs: I,
                     precedence: number, part?: Writer, step: number = 1): Writer {
    if (step === 1) {
      if (part === void 0) {
        part = Unicode.writeString(operator, output);
      } else {
        part = part.pull(output);
      }
      if (part.isDone()) {
        part = void 0;
        step = 2;
      } else if (part.isError()) {
        return part.asError();
      }
    }
    if (step === 2) {
      if (recon.precedence(rhs) < precedence) {
        if (output.isCont()) {
          output = output.write(40/*'('*/);
          step = 3;
        }
      } else {
        step = 3;
      }
    }
    if (step === 3) {
      if (part === void 0) {
        part = recon.writeItem(rhs, output);
      } else {
        part = part.pull(output);
      }
      if (part.isDone()) {
        part = void 0;
        step = 4;
      } else if (part.isError()) {
        return part.asError();
      }
    }
    if (step === 4) {
      if (recon.precedence(rhs) < precedence) {
        if (output.isCont()) {
          output = output.write(41/*')'*/);
          return Writer.done();
        }
      } else {
        return Writer.done();
      }
    }
    if (output.isDone()) {
      return Writer.error(new WriterException("truncated"));
    } else if (output.isError()) {
      return Writer.error(output.trap());
    }
    return new PrefixOperatorWriter<I, V>(recon, operator, rhs, precedence, part, step);
  }
}
