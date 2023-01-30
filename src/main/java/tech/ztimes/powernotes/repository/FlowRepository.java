package tech.ztimes.powernotes.repository;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.intellij.openapi.application.ApplicationManager;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.message.FlowListener;

import java.util.List;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.equal;

public class FlowRepository {
    protected final static IndexedCollection<Flow> repo = new ConcurrentIndexedCollection<>();
    public static final Attribute<Flow, Long> ID = attribute(Flow.class, Long.class, "id", Flow::getId);

    public FlowRepository() {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(FlowListener.TOPIC, new FlowListener() {
            @Override
            public void save(Flow flow) {
                FlowRepository.this.save(flow);
            }

            @Override
            public void remove(Flow flow) {
                FlowRepository.this.remove(flow);
            }
        });
    }

    public static FlowRepository getInstance() {
        return ApplicationManager.getApplication().getService(FlowRepository.class);
    }

    public Flow get(Long id) {
        var query = equal(ID, id);

        try (var resultSet = repo.retrieve(query)) {
            return resultSet.stream().findFirst().orElse(null);
        }
    }

    public List<Flow> list() {
        return repo.stream().collect(Collectors.toList());
    }

    public void save(Flow flow) {
        repo.remove(flow);
        repo.add(flow);
    }

    public void remove(Flow flow) {
        var query = equal(ID, flow.getId());

        try (var resultSet = repo.retrieve(query)) {
            resultSet.stream().forEach(repo::remove);
        }
    }
}
