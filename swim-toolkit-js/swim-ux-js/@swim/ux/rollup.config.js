import nodeResolve from "@rollup/plugin-node-resolve";
import sourcemaps from "rollup-plugin-sourcemaps";

const script = "swim-ux";
const namespace = "swim";

const main = {
  input: "./lib/main/index.js",
  output: {
    file: `./dist/main/${script}.js`,
    name: namespace,
    format: "umd",
    globals: {
      "@swim/util": "swim",
      "@swim/codec": "swim",
      "@swim/interpolate": "swim",
      "@swim/structure": "swim",
      "@swim/math": "swim",
      "@swim/time": "swim",
      "@swim/uri": "swim",
      "@swim/angle": "swim",
      "@swim/length": "swim",
      "@swim/color": "swim",
      "@swim/font": "swim",
      "@swim/shadow": "swim",
      "@swim/transform": "swim",
      "@swim/scale": "swim",
      "@swim/transition": "swim",
      "@swim/animate": "swim",
      "@swim/style": "swim",
      "@swim/render": "swim",
      "@swim/constraint": "swim",
      "@swim/view": "swim",
      "@swim/gesture": "swim",
    },
    sourcemap: true,
    interop: false,
    extend: true,
  },
  external: [
    "@swim/util",
    "@swim/codec",
    "@swim/interpolate",
    "@swim/structure",
    "@swim/math",
    "@swim/time",
    "@swim/uri",
    "@swim/angle",
    "@swim/length",
    "@swim/color",
    "@swim/font",
    "@swim/shadow",
    "@swim/transform",
    "@swim/scale",
    "@swim/transition",
    "@swim/animate",
    "@swim/style",
    "@swim/render",
    "@swim/constraint",
    "@swim/view",
    "@swim/gesture",
  ],
  plugins: [
    nodeResolve({customResolveOptions: {paths: "../.."}}),
    sourcemaps(),
  ],
  onwarn(warning, warn) {
    if (warning.code === "CIRCULAR_DEPENDENCY") return;
    warn(warning);
  },
};

const targets = [main];
targets.main = main;
export default targets;
