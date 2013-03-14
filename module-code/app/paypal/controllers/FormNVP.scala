/*
 * Copyright 2013 Micronautics Research Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package paypal.controllers

class FormNVP(form: Map[String, Seq[String]]) {

  def get(name: String): Seq[String] = {
    val strArray: Seq[String] = (for {
      strings <- form.get(name).toSeq
      string <- strings.toSeq
    } yield string).toSeq
    val result: Seq[String] = if (strArray.size==1 && strArray.head=="") Nil else strArray
    result
  }

  /** @return first element of form field as an Option[String] */
  def maybeGetString(name: String): Option[String] = get(name).headOption
}
