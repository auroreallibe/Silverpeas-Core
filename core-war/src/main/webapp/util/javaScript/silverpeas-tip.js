/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

  /**
   * Tip Manager plugin.
   * It handles the display of tips.
   * @constructor
   */
  window.TipManager = new function () {
    var _computeParams = function(parameters) {
      var params = parameters ? parameters : {};
      params = extendsObject({
        common : {
          style : {
            zindex : -1
          }
        },
        prerender : true,
        style : {
          classes : "qtip-shadow"
        },
        content : {
          title : ''
        },
        position : {
          my : "center left",
          at : "center right",
          adjust : {
            method : "flipinvert"
          },
          viewport : jQuery(window)
        },
        show : {
          solo: true,
          delay : 250
        },
        hide : {
          event : 'mouseleave'
        }
      }, params);

      if (params.common.style.zindex !== -1) {
        $.fn.qtip.zindex = params.common.style.zindex;
      }

      return params;
    };

    var _performQTip = function(element, content, options, qtipOptions) {
      var contentType = typeof content;
      if (contentType !== 'string' && contentType !== 'function') {
        qtipOptions.content = jQuery(content);
      }
      if (options && options.style && typeof options.style.classes === 'string') {
        qtipOptions.style.classes = qtipOptions.style.classes + ' ' + options.style.classes;
      }
      return jQuery(element).qtip(qtipOptions).qtip('api');
    };

    /**
     * Displays as a simple way an help represented as a tip.
     * @param element the element on which the qtip must be applied.
     * @param message the text message.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleHelp = function(element, message, options) {
      var params = options ? options : {};
      var qtipOptions = _computeParams(extendsObject(params, {
        style : {
          classes : "qtip-shadow qtip-default-silverpeas"
        },
        content : {
          text : message
        }
      }));
      return _performQTip(element, undefined, options, qtipOptions);
    };

    /**
     * Displays a simple way information into represented as a tip.
     * @param element the element on which the qtip must be applied.
     * @param message the text message.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleInfo = function(element, message, options) {
      var params = options ? options : {};
      var qtipOptions = _computeParams(extendsObject(params, {
        style : {
          classes : "qtip-shadow qtip-default-silverpeas qtip-info"
        },
        content : {
          text : message
        }
      }));
      return _performQTip(element, undefined, options, qtipOptions);
    };

    /**
     * Displays a simple way information into represented as a tip.
     * @param element the element on which the qtip must be applied.
     * @param content the content.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleSelect = function(element, content, options) {
      var params = options ? options : {};
      var qtipOptions = _computeParams(extendsObject({
        show : {
          event : 'click'
        },
        hide : {
          event : 'unfocus'
        }
      }, params, {
        style : {
          classes : "qtip-shadow qtip-light qtip-default-silverpeas qtip-select"
        },
        content : {
          text : content
        }
      }));
      qtipOptions.position = undefined;
      return _performQTip(element, content, options, qtipOptions);
    };

    /**
     * Displays a simple way information into represented as a tip.
     * @param element the element on which the qtip must be applied.
     * @param content the content.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleDetails = function(element, content, options) {
      var params = options ? options : {};
      var qtipOptions = _computeParams(extendsObject({
        show : {
          event : 'click'
        },
        hide : {
          event : 'unfocus'
        }
      }, params, {
        style : {
          tip : true,
          classes : "qtip-shadow qtip-bootstrap qtip-default-silverpeas qtip-details"
        },
        content : {
          text : content
        },
        position : {
          adjust : {
            method : "flip flip"
          },
          at : "top right",
          my : "bottom right"
        }
      }));
      return _performQTip(element, content, options, qtipOptions);
    };

    /**
     * Destroys all qTip representations belonging to the selected elements.
     * @param selector the CSS selector.
     */
    this.destroyAll = function(selector) {
      jQuery(selector).qtip('destroy', true);
    };

    /**
     * Hides all qTip representations belonging to the selected elements.
     * @param selector the CSS selector.
     */
    this.hideAll = function(selector) {
      jQuery(selector).qtip('toggle', false);
    };

    /**
     * Shows all qTip representations belonging to the selected elements.
     * @param selector the CSS selector.
     */
    this.showAll = function(selector) {
      jQuery(selector).qtip('toggle', true);
    };
  };
})();
