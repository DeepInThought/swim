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

package swim.io;

public class TcpSocketSpec extends SocketBehaviors {
  final SocketSettings socketSettings = SocketSettings.standard();

  @Override
  protected ServiceRef bind(Endpoint endpoint, Service service) {
    return endpoint.bindTcp("127.0.0.1", 53556, service, this.socketSettings);
  }

  @Override
  protected SocketRef connect(Endpoint endpoint, Socket socket) {
    return endpoint.connectTcp("127.0.0.1", 53556, socket, this.socketSettings);
  }
}