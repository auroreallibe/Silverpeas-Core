/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

/**
 * Silverpeas plugin build upon JQuery to display a modal dialog box.
 * It uses the JQuery UI framework.
 */
(function($) {

  var __displayFullscreenModalBackground = (top !== window && top.spLayout);

  $.popup = {
    debug : false,
    initialized: false,
    doInitialize: function() {
      if (!$.popup.initialized) {
        window.i18n.properties({
          name: 'generalMultilang',
          path: webContext + '/services/bundles/org/silverpeas/multilang/',
          language: '$$', /* by default the language of the user in the current session */
          mode: 'map'
        });
        $.popup.initialized = true;
      }
    },
    showWaiting: function() {
      var $waiting = $("#spWaiting");
      if ($waiting.length === 0) {
        $waiting = $("<div>").attr('id', 'spWaiting').attr('style',
                'display: none; border: 0; padding: 0; text-align: center; overflow: hidden;');
        $(document.body).append($waiting);
        $waiting.popup("waiting");

        // Little hack to prevent some unexpected errors when escape key is
        // pressed during an ajax request
        $waiting.dialog("widget").keydown(function(e) {
          if (e.keyCode === 27) {
            e.preventDefault();
          }
        });
      }
      $waiting.dialog("open");
    },
    hideWaiting: function() {
      var $waiting = $("#spWaiting");
      if ($waiting.length > 0) {
        $waiting.dialog("close");
        $waiting.dialog("destroy");
        $waiting.remove();
      }
    },
    confirm: function(message, params) {
      var options = params;
      var $confirm = $('<div>').append($('<p>').append(message));
      if (typeof params === 'function') {
        options = {
          callback: params
        }
      }
      $confirm.popup('confirmation', options);
    },
    error: function(message, params) {
      var options = params;
      message = message.replaceAll('\n', '<br/>');
      var $error = $('<div>').append($('<p>').append(message));
      if (typeof params === 'function') {
        options = {
          callback: params
        }
      }
      $error.popup('error', options);
    }
  };

  /**
   * The different methods on messages handled by the plugin.
   */
  var methods = {
    /**
     * Close the current popup
     */
    close : function() {
      $(this).dialog('close');
    },
    /**
     * The modal free dialog : configure as you want your popup.
     */
    free: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      settings = $.extend(__buildInternalSettings({
        buttonDisplayed: false,
        width: 'auto'
      }), settings);

      if (__isIE7()) {
        // Width & Height
        if (options.width) {
          settings.width = options.width;
        }
        if (options.height) {
          settings.height = eval(options.height) + 27;
        }
      }

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal basic dialog. (scroll is deactivated)
     * It accepts one parameter that is an object with following attributes:
     * - title : the document title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing,
     * - width : width of content. Mandatory for IE7 browser and ignored in other cases,
     * - height : height of content. Mandatory for IE7 browser and ignored in other cases.
     */
    basic: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonDisplayed: false,
        disabledParentScroll: true,
        width: 'auto'
      }));

      if (__isIE7()) {
        // Width & Height
        if (options.width) {
          settings.width = options.width;
        }
        if (options.height) {
          settings.height = eval(options.height) + 27;
        }
      }

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal information dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing.
     */
    information: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = window.i18n.prop('GML.information.dialog.title');
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextNo: window.i18n.prop('GML.ok'),
        isMaxWidth: true
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal error dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing.
     */
    error: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = window.i18n.prop('GML.error.dialog.title');
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextNo: window.i18n.prop('GML.ok'),
        isMaxWidth: true
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal help dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing.
     */
    help: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = window.i18n.prop('GML.help.dialog.title');
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextNo: window.i18n.prop('GML.ok'),
        isMaxWidth: true,
        dialogClass: 'help-modal-message'
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal validation dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing.
     */
    validation: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      settings = $.extend(__buildInternalSettings({
        buttonTextYes: window.i18n.prop('GML.validate'),
        buttonTextNo: window.i18n.prop('GML.cancel'),
        isMaxWidth: true
      }), settings);

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal confirmation dialog.
     * A warning icon is automatically inserted into the title bar.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing.
     */
    confirmation: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = window.i18n.prop('GML.confirmation.dialog.title');
      }
      var $title = $('<div>').attr('style', 'display: table;');
      var $titleRow = $('<div>').attr('style', 'display: table-row;');
      $title.append($titleRow);
      var $icon = $('<span>').addClass('ui-icon ui-icon-alert');
      $icon.attr('style', 'float:left; margin:0 7px 0 0;');
      var $titleText = $('<div>').attr('style', 'display: table-cell;vertical-align: bottom;');
      $titleText.html(settings.title);
      $titleRow.append($('<div>').attr('style',
              'display: table-cell;vertical-align: middle;').append($icon));
      $titleRow.append($titleText);
      settings.title = $('<div>').append($title).html();

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextYes: window.i18n.prop('GML.yes'),
        buttonTextNo: window.i18n.prop('GML.no'),
        isMaxWidth: true
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal preview dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the document title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing,
     * - width : width of content. Mandatory for IE7 browser and ignored in other cases,
     * - height : height of content. Mandatory for IE7 browser and ignored in other cases.
     */
    preview: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      settings.title = window.i18n.prop('GML.preview.dialog.title');
      if (options.title && options.title.length > 0) {
        settings.title =
                settings.title + " " + window.i18n.prop('GML.preview.dialog.title.of') + " " + options.title;
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonDisplayed: false,
        width: 'auto'
      }));

      if (__isIE7()) {
        // Width & Height
        if (options.width) {
          settings.width = options.width;
        }
        if (options.height) {
          settings.height = eval(options.height) + 27;
        }
      }

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal view dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the document title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed,
     * - callbackOnClose : the callback on dialog box closing,
     * - width : width of content. Mandatory for IE7 browser and ignored in other cases,
     * - height : height of content. Mandatory for IE7 browser and ignored in other cases.
     */
    view: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      settings.title = window.i18n.prop('GML.view.dialog.title');
      if (options.title && options.title.length > 0) {
        settings.title =
                settings.title + " " + window.i18n.prop('GML.view.dialog.title.of') + " " + options.title;
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonDisplayed: false,
        width: 'auto'
      }));

      if (__isIE7()) {
        // Width & Height
        if (options.width) {
          settings.width = options.width;
        }
        if (options.height) {
          settings.height = eval(options.height) + 27;
        }
      }

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal waiting dialog.
     */
    waiting: function() {
      var $container = $(this);

      // Common settings
      var settings = __extendCommonSettings({});

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        displayTitle: false,
        closeOnEscape: false,
        buttonDisplayed: false,
        width: "32px",
        height: 39
      }));

      // Waiting animation
      var imageUrl = popupViewGeneratorIconPath + '/inProgress.gif';
      $container.html($('<img>').attr('src', imageUrl).attr('width', '32').attr('height', '32'));

      // Dialog
      return __openPopup($container, settings);
    }
  };

  /**
   * The modal dialog box Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the popup namespace in JQuery in which methods on messages are provided.
   */
  $.fn.popup = function(method) {
    $.popup.doInitialize();
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.validation.apply(this, arguments);
    } else {
      return $.error('Method ' + method + ' does not exist on jQuery.popup');
    }
  };

  /**
   * Private function that centralizes extension of common settings
   */
  function __extendCommonSettings(options) {
    var settings = {
      title: '',
      callback: null,
      alternativeCallback: null,
      callbackOnClose: null
    };
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that centralizes extension of internal settings
   */
  function __buildInternalSettings(options) {
    var settings = {
      displayTitle: true,
      closeOnEscape: true,
      disabledParentScroll: false,
      buttonDisplayed: true,
      buttonTextYes: '',
      buttonTextNo: '',
      isMaxWidth: false,
      width: 570,
      height: 'auto',
      dialogClass: ''
    };
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that checks if the browser is an IE7 one
   */
  function __isIE7() {
    return (navigator.appVersion.indexOf("MSIE 7.") !== -1);
  }

  /**
   * Private function that centralizes a dialog creation and its opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openPopup($this, options) {
    if (!$this.length) {
      return $this;
    }

    return $this.each(function() {
      var $_this = $(this);
      if (!options.title) {
        options.title = $_this.attr('title');
      }

      $_this.dialog({
        closeOnEscape: options.closeOnEscape,
        title: options.title,
        autoOpen: false,
        modal: true,
        resizable: false,
        height: options.height,
        dialogClass: options.dialogClass
      });

      // Removing the title if requested
      if (!options.displayTitle) {
        $_this.dialog('widget').find(".ui-dialog-titlebar").hide();
      }

      // Buttons
      if (options.buttonDisplayed) {
        var buttons = [];
        if (options.buttonTextYes) {
          buttons.push({
            text: options.buttonTextYes,
            click: function() {
              var isok = true;
              if (options.callback) {
                isok = options.callback.call($_this);
              }
              if (isok || isok === undefined) {
                $_this.dialog("close");
              }
            }
          });
        }
        if (options.buttonTextNo) {
          buttons.push({
            text: options.buttonTextNo,
            click: function() {
              if (options.alternativeCallback) {
                options.alternativeCallback.call($_this);
              }
              $_this.dialog("close");
            }
          });
        }
        if (buttons.length > 0) {
          $_this.dialog("option", "buttons", buttons);
        }
      }

      // Callback on close
      $_this.dialog("option", "close", function(event, ui) {
        if (options.callbackOnClose) {
          options.callbackOnClose.call(this);
        }
      });

      // Scroll
      if (options.disabledParentScroll) {
        $_this.dialog("option", "open", function(event, ui) {
          $("html,body").css("overflow", "hidden");
        });
        $_this.dialog("option", "beforeClose", function(event, ui) {
          $("html,body").css("overflow", "auto");
        });
      }

      // Width
      var width = "" + options.width;
      width = (width !== 'auto') ? width.replace(/px/, '') + 'px' : width;
      if (options.isMaxWidth) {
        $_this.dialog("option", "width", "auto");
      } else {
        $_this.dialog("option", "width", width);
      }

      // Dialog opening
      var $dialog = $_this.dialog('open');

      // Since JQuery upgrade (jquery-1.10.1.min.js), HTML code in title is escaped.
      // The below code surrounds this problem.
      // For new version of jquery, please verify if it can be removed.
      $dialog.data("uiDialog")._title = function(title) {
        title.html(this.options.title);
      };
      $dialog.dialog('option', 'title', options.title);

      // This below code handles the width of the dialog after it has been displayed.
      if (options.isMaxWidth) {
        // If max width is required, resizing and repositioning after the dialog open
        $_this.dialog("widget").css('max-width', width);
        $_this.dialog({position : $_this.dialog('option', 'position')});
      }
    });
  }

  var spFullscreenModalBackgroundContext = new function() {
    if (!top.window.__spFullscreenModalBackgroundContext) {
      top.window.__spFullscreenModalBackgroundContext = {count : 0};
    }
    var __localSpFullscreenModalBackgroundContext = {count : 0};
    this.isValid = function() {
      return top.window.__spFullscreenModalBackgroundContext.count ===
          __localSpFullscreenModalBackgroundContext.count;
    };
    this.clear = function() {
      top.window.__spFullscreenModalBackgroundContext.count = 0;
      __localSpFullscreenModalBackgroundContext.count = 0;
      var $container = this.getContainer();
      if ($container.length > 0) {
        try {
          $container.dialog("close");
          $container.dialog("destroy");
        } catch (e) {
          __logDebug(e);
          __logDebug("cleaning manually jQuery.ui.dialog");
          var $domToClear = $("div[aria-describedby=spFullscreenModalBackground]", top.document);
          if ($domToClear.length) {
            var zIndexToTarget = "" + ($domToClear[0].style.zIndex - 1);
            $(".ui-widget-overlay", top.document).each(function(i, overlay) {
              if (overlay.style.zIndex === zIndexToTarget) {
                $(overlay).remove();
              }
            });
            $domToClear.remove();
          }
        }
        $container.remove();
        spLayout.getBody().getContent().setOnBackground();
      }
    };
    this.increase = function() {
      top.window.__spFullscreenModalBackgroundContext.count++;
      __localSpFullscreenModalBackgroundContext.count++;
    };
    this.decrease = function() {
      top.window.__spFullscreenModalBackgroundContext.count--;
      __localSpFullscreenModalBackgroundContext.count--;
      if (__localSpFullscreenModalBackgroundContext.count < 0) {
        this.clear();
      }
    };
    this.getContainer = function() {
      return $("#spFullscreenModalBackground", top.document);
    };
    this.isFirst = function() {
      return __localSpFullscreenModalBackgroundContext.count === 1;
    };
  };

  /**
   * Private function that centralizes a fullscreen modal background.
   * Be careful, options have to be well initialized before this function call
   */
  var __lastRegisteredHandler;
  function __openFullscreenModalBackground($dialogInstance) {
    if (!spFullscreenModalBackgroundContext.isValid()) {
      spFullscreenModalBackgroundContext.clear();
    }
    spFullscreenModalBackgroundContext.increase();

    if (spFullscreenModalBackgroundContext.isFirst()) {
      var $container = $("<div>").attr('id', 'spFullscreenModalBackground').attr('style',
          'display: none; border: 0; padding: 0; height: 0; width: 0; overflow: hidden;');
      $(top.document.body).append($container);

      $container.dialog({
        fullscreenModalBackground : true,
        closeOnEscape : false,
        autoOpen : false,
        modal : true,
        resizable : false,
        height : '0px',
        width : '0px'
      });

      $container.dialog('widget').find(".ui-dialog-titlebar").hide();

      // Little hack to prevent some unexpected errors when escape key is
      // pressed during an ajax request
      $container.dialog("widget").keydown(function(e) {
        if (e.keyCode === 27) {
          e.preventDefault();
        }
      });

      // Handling HTML forms in order to close the dialog on submit action.
      // As jQuery Handles only jQuery triggering, jQuery method and the standard one must be
      // managed.
      var $forms = $('form', $dialogInstance);
      var forms = $dialogInstance[0].querySelectorAll("form");
      if (__lastRegisteredHandler) {
        $forms.unbind('submit', __lastRegisteredHandler);
        [].slice.call(forms, 0).forEach(function(form) {
          form.removeEventListener('submit', __lastRegisteredHandler);
        });
      }
      __lastRegisteredHandler = function() {
        if ($container.dialog('isOpen')) {
          $container.dialog("close");
        }
        return true;
      };
      $forms.bind('submit', __lastRegisteredHandler);
      [].slice.call(forms, 0).forEach(function(form) {
        form.addEventListener('submit', __lastRegisteredHandler);
      });

      // Displaying the dialog.
      spLayout.getBody().getContent().setOnForeground();
      $container.dialog("open");
      $container.dialog("widget").css('top', '-1000px').css('left', '-1000px');
    }
  }

  function __closeFullscreenModalBackground() {
    if (spFullscreenModalBackgroundContext.isFirst() ||
        !spFullscreenModalBackgroundContext.isValid()) {
      spFullscreenModalBackgroundContext.clear();
    }
    spFullscreenModalBackgroundContext.decrease();
  }

  function __adjustPosition(dialogOptions) {
    var position = dialogOptions.position;
    if (position.my === "center" && position.at === "center" && position.of === window) {
      var headerHeightOffset = spLayout.getHeader().getContainer().offsetHeight / 2;
      var isContentFullWidth = !($(spLayout.getBody().getContent().getContainer()).position().left);
      if (isContentFullWidth) {
        var navigationHeightOffset = spLayout.getBody().getNavigation().getContainer().offsetHeight / 2;
        position.at = "center center-" + (headerHeightOffset + navigationHeightOffset);
      } else {
        var navigationWidthOffset = spLayout.getBody().getNavigation().getContainer().offsetWidth / 2;
        position.at = "center-" + navigationWidthOffset + " center-" + headerHeightOffset;
      }
    }
    return position;
  }

  $.widget("ui.dialog", $.ui.dialog, {
    open : function() {
      if (__displayFullscreenModalBackground && !this._isOpen &&
          !this.options.fullscreenModalBackground) {
        __adjustPosition(this.options);
        __openFullscreenModalBackground(this.element);
      }
      return this._super();
    },
    close : function() {
      if (__displayFullscreenModalBackground && this._isOpen &&
          !this.options.fullscreenModalBackground) {
        __closeFullscreenModalBackground();
      }
      return this._super();
    },
    destroy : function() {
      if (__displayFullscreenModalBackground && this._isOpen &&
          !this.options.fullscreenModalBackground) {
        __closeFullscreenModalBackground();
      }
      return this._super();
    }
  });

  function __logDebug(message) {
    if ($.popup.debug) {
      sp.log.debug("Popup - " + message);
    }
  }

  if (__displayFullscreenModalBackground) {
    __logDebug("cleaning popup from iframe");
    spFullscreenModalBackgroundContext.clear();
  }

})(jQuery);

/**
 * Some helpers
 */

/**
 * Load html from URL and display it in a popup.
 * If this method is called several times from a same Page,
 * the previous load is trashed and replaced by the new one.
 * @param url
 * @param options
 * @return promise with the html data loaded.
 */
function displaySingleFreePopupFrom(url, options) {
  var deferred = new jQuery.Deferred();
  jQuery.popup.showWaiting();
  jQuery.ajax({
    url: url,
    type: 'GET',
    dataType: 'html',
    cache : false
  }).success(function(data, status, jqXHR) {
    var $popup = jQuery('#popupHelperContainer');
    if ($popup.length == 0) {
      $popup = jQuery('<div>', {'id' : 'popupHelperContainer', 'style' : 'display: none'});
      $popup.appendTo(document.body);
    }
    $popup.empty();
    $popup.append(data);
    $popup.popup('free', options);
    deferred.resolve(data);
  }).error(function(jqXHR, textStatus, errorThrown) {
    notyError(errorThrown);
    deferred.reject();
  }).always(function(data, status, jqXHR) {
    jQuery.popup.hideWaiting();
  });
  return deferred.promise();
}

/**
 * Closes the single free popup.
 */
function closeSingleFreePopup() {
  jQuery('#popupHelperContainer').popup('close');
}

/**
 * Load html from URL and display it in a popup.
 * If this method is called several times from a same Page,
 * the previous load is trashed and replaced by the new one.
 * @param url
 * @param options
 * @return promise with the html data loaded.
 */
function displaySingleConfirmationPopupFrom(url, options) {
  var deferred = new jQuery.Deferred();
  jQuery.popup.showWaiting();
  jQuery.ajax({
    url: url,
    type: 'GET',
    dataType: 'html',
    cache : false
  }).success(function(data, status, jqXHR) {
    var $popup = jQuery('#popupHelperContainer');
    if ($popup.length == 0) {
      $popup = jQuery('<div>', {'id' : 'popupHelperContainer', 'style' : 'display: none'});
      $popup.appendTo(document.body);
    }
    $popup.empty();
    $popup.append(data);
    $popup.popup('confirmation', options);
    deferred.resolve(data);
  }).error(function(jqXHR, textStatus, errorThrown) {
    notyError(errorThrown);
    deferred.reject();
  }).always(function(data, status, jqXHR) {
    jQuery.popup.hideWaiting();
  });
  return deferred.promise();
}

/**
 * Closes the single confirmation popup.
 */
function closeSingleConfirmationPopup() {
  jQuery('#popupHelperContainer').popup('close');
}