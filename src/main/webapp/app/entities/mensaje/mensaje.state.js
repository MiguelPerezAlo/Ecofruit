(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('mensaje', {
            parent: 'entity',
            url: '/mensaje?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ecofruitApp.mensaje.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mensaje/mensajes.html',
                    controller: 'MensajeController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('mensaje');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('mensaje.enviados', {
            parent: 'entity',
            url: '/mensajesEnviados?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ecofruitApp.mensaje.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mensaje/mensajesEnviados.html',
                    controller: 'MensajeController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('mensaje');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('mensaje-detail', {
            parent: 'entity',
            url: '/mensaje/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ecofruitApp.mensaje.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mensaje/mensaje-detail.html',
                    controller: 'MensajeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('mensaje');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Mensaje', function($stateParams, Mensaje) {
                    return Mensaje.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('mensaje.new', {
            parent: 'mensaje',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mensaje/mensaje-dialog.html',
                    controller: 'MensajeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                titulo: null,
                                contenido: null,
                                fecha: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('mensaje', null, { reload: true });
                }, function() {
                    $state.go('mensaje');
                });
            }]
        })
        .state('mensaje.edit', {
            parent: 'mensaje',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mensaje/mensaje-dialog.html',
                    controller: 'MensajeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Mensaje', function(Mensaje) {
                            return Mensaje.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('mensaje', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('mensaje.delete', {
            parent: 'mensaje',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mensaje/mensaje-delete-dialog.html',
                    controller: 'MensajeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Mensaje', function(Mensaje) {
                            return Mensaje.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('mensaje', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
