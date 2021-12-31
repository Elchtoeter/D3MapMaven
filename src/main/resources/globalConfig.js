  var pr  = require('./node_modules/process/browser.js');
  const { TextEncoder } = require('@sinonjs/text-encoding')
  const { TextDecoder } = require('@sinonjs/text-encoding')
  const { Buffer } = require('buffer');
  
  globalThis.setTimeout = function(){};
  globalThis.Buffer = Buffer;
  globalThis.TextEncoder = TextEncoder;
  globalThis.TextDecoder = TextDecoder;
  globalThis.process = pr;

  