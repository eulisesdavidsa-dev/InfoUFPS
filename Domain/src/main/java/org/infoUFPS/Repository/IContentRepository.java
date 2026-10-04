package org.infoUFPS.Repository;

import org.infoUFPS.Model.Content;
import org.infoUFPS.Model.ContentId;
import org.infoUFPS.Model.Status;

import java.util.List;
import java.util.Optional;

public interface IContentRepository {

    public Content save(Content content);

    public Optional<Content> findById(ContentId id);

    public List<Content> findAllPublished();

    public List<Content> findByStatus(Status status);

    public void deleteById(ContentId id);

    public boolean existsById(ContentId id);
}
