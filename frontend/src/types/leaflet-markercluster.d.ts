import 'leaflet';

declare module 'leaflet' {
  function markerClusterGroup(options?: MarkerClusterGroupOptions): MarkerClusterGroup;

  interface MarkerClusterGroupOptions {
    chunkedLoading?: boolean;
    chunkInterval?: number;
    chunkDelay?: number;
    maxClusterRadius?: number | ((zoom: number) => number);
    disableClusteringAtZoom?: number;
  }

  interface MarkerClusterGroup extends FeatureGroup {
    addLayers(layers: Layer[]): this;
    getLayers(): Layer[];
  }
}
